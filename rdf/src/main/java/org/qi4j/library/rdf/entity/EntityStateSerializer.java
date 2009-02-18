/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.rdf.entity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rdf.Qi4jEntity;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.QualifierQualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.value.CompoundType;
import org.qi4j.spi.value.PrimitiveType;

/**
 * TODO
 */
public class EntityStateSerializer
{

    private Map<String, URI> dataTypes = new HashMap<String, URI>();

    public EntityStateSerializer()
    {
        // TODO A ton more types need to be added here
        dataTypes.put( String.class.getName(), XMLSchema.STRING );
        dataTypes.put( Integer.class.getName(), XMLSchema.INT );
        dataTypes.put( Boolean.class.getName(), XMLSchema.BOOLEAN );
        dataTypes.put( Byte.class.getName(), XMLSchema.BYTE );
        dataTypes.put( BigDecimal.class.getName(), XMLSchema.DECIMAL );
        dataTypes.put( Double.class.getName(), XMLSchema.DOUBLE );
        dataTypes.put( Long.class.getName(), XMLSchema.LONG );
        dataTypes.put( Short.class.getName(), XMLSchema.SHORT );
        dataTypes.put( Date.class.getName(), XMLSchema.DATETIME );
    }

    public Iterable<Statement> serialize( final EntityState entityState )
    {
        return serialize( entityState, true );
    }

    public Iterable<Statement> serialize( final EntityState entityState,
                                          final boolean includeNonQueryable )
    {
        Graph graph = new GraphImpl();
        serialize( entityState, includeNonQueryable, graph );
        return graph;
    }

    public void serialize( final EntityState entityState,
                           final boolean includeNonQueryable,
                           final Graph graph )
    {
        if( !( includeNonQueryable || entityState.entityType().queryable() ) )
        {
            return; // Not queryable, and should not be included
        }

        QualifiedIdentity qid = entityState.qualifiedIdentity();
        ValueFactory values = graph.getValueFactory();
        URI entityUri = values.createURI( qid.toURI() );
        EntityType entityType = entityState.entityType();

        graph.add( entityUri, Rdfs.TYPE, values.createURI( entityType.toURI() ) );

        serializeProperties( entityState, graph, entityUri, entityType.properties(), includeNonQueryable );
        serializeAssociations( entityState, graph, entityUri, entityType.associations(), includeNonQueryable );
        serializeManyAssociations( entityState, graph, entityUri, entityType.manyAssociations(), includeNonQueryable );
    }

    private void serializeProperties( final EntityState entityState,
                                      final Graph graph,
                                      final URI entityUri,
                                      final Iterable<PropertyType> properties,
                                      final boolean includeNonQueryable )
    {
        ValueFactory values = graph.getValueFactory();

        // Properties
        for( PropertyType propertyType : properties )
        {
            if( !( includeNonQueryable || propertyType.queryable() ) )
            {
                continue; // Skip non-queryable
            }
            Object value = entityState.getProperty( propertyType.qualifiedName() );
            if( value != null )
            {
                System.out.println( propertyType.type().getClass() );
                if( propertyType.type() instanceof PrimitiveType )
                {
                    URI propertyUri = values.createURI( propertyType.uri() );
                    Literal rdfValue = values.createLiteral( value.toString() );
                    graph.add( entityUri, propertyUri, rdfValue );
                }
                else if( propertyType.type() instanceof CompoundType )
                {
                    System.out.println( value );
                }
                else
                {
                    throw new UnsupportedOperationException(
                        "RDF serialization of property type " + propertyType.type().getClass().getName()
                        + " not supported (" + propertyType.qualifiedName() + ")"
                    );
                }
            }
        }
    }

    private void serializeAssociations( final EntityState entityState,
                                        final Graph graph, URI entityUri,
                                        final Iterable<AssociationType> associations,
                                        final boolean includeNonQueryable )
    {
        ValueFactory values = graph.getValueFactory();

        // Associations
        for( AssociationType associationType : associations )
        {
            if( !( includeNonQueryable || associationType.queryable() ) )
            {
                continue; // Skip non-queryable
            }

            QualifiedIdentity associatedId = entityState.getAssociation( associationType.qualifiedName() );
            if( associatedId != null )
            {
                URI assocURI = values.createURI( associationType.uri() );
                if( associatedId instanceof QualifierQualifiedIdentity )
                {
                    QualifierQualifiedIdentity arqi = (QualifierQualifiedIdentity) associatedId;
                    serializeQualifier( graph, arqi, assocURI, entityUri );

                }
                else
                {
                    URI assocEntityURI = values.createURI( associatedId.toURI() );
                    graph.add( entityUri, assocURI, assocEntityURI );
                }
            }
        }
    }

    private void serializeManyAssociations( final EntityState entityState,
                                            final Graph graph,
                                            final URI entityUri,
                                            final Iterable<ManyAssociationType> associations,
                                            final boolean includeNonQueryable )
    {
        ValueFactory values = graph.getValueFactory();

        // Many-Associations
        for( ManyAssociationType associationType : associations )
        {
            if( !( includeNonQueryable || associationType.queryable() ) )
            {
                continue; // Skip non-queryable
            }

            BNode collection = values.createBNode();
            graph.add( entityUri, values.createURI( associationType.uri() ), collection );
            if( associationType.associationType() == ManyAssociationType.ManyAssociationTypeEnum.LIST )
            {
                graph.add( collection, Rdfs.TYPE, Rdfs.SEQ );
            }
            else
            {
                graph.add( collection, Rdfs.TYPE, Rdfs.BAG );
            }

            Collection<QualifiedIdentity> associatedIds = entityState.getManyAssociation( associationType.qualifiedName() );
            for( QualifiedIdentity associatedId : associatedIds )
            {
                if( associatedId instanceof QualifierQualifiedIdentity )
                {
                    QualifierQualifiedIdentity arqi = (QualifierQualifiedIdentity) associatedId;
                    serializeQualifier( graph, arqi, Rdfs.LIST_ITEM, collection );
                }
                else
                {
                    URI assocEntityURI = values.createURI( associatedId.toURI() );
                    graph.add( collection, Rdfs.LIST_ITEM, assocEntityURI );
                }
            }
        }
    }

    public Iterable<Statement> serialize( final EntityType entityType )
    {
        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        URI entityTypeUri = values.createURI( entityType.toURI() );

        graph.add( entityTypeUri, Rdfs.TYPE, Rdfs.CLASS );
        graph.add( entityTypeUri, Rdfs.TYPE, OWL.CLASS );
        serializeMixinTypes( entityType, graph, entityTypeUri );


        serializePropertyTypes( entityType, graph, entityTypeUri );
        serializeAssociationTypes( entityType, graph, entityTypeUri );
        serializeManyAssociationTypes( entityType, graph, entityTypeUri );

        return graph;
    }

    private void serializeMixinTypes( final EntityType entityType,
                                      final Graph graph,
                                      final URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();

        // Subclass of itself
        graph.add( entityTypeUri, Rdfs.SUB_CLASS_OF, entityTypeUri );

        // Mixin types
        for( String mixinType : entityType.mixinTypes() )
        {
            if( !mixinType.equals( entityType.type() ) )
            {
                graph.add( entityTypeUri, Rdfs.SUB_CLASS_OF, values.createURI( Classes.toURI( mixinType ) ) );
            }
        }
    }

    private void serializeManyAssociationTypes( final EntityType entityType,
                                                final Graph graph,
                                                final URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();
        // ManyAssociations
        for( ManyAssociationType manyAssociationType : entityType.manyAssociations() )
        {
            URI associationURI = values.createURI( manyAssociationType.uri() );
            graph.add( associationURI, Rdfs.DOMAIN, entityTypeUri );

            if( manyAssociationType.associationType() == ManyAssociationType.ManyAssociationTypeEnum.LIST )
            {
                graph.add( associationURI, Rdfs.TYPE, Rdfs.SEQ );
            }
            else if( manyAssociationType.associationType() == ManyAssociationType.ManyAssociationTypeEnum.SET )
            {
                graph.add( associationURI, Rdfs.TYPE, Rdfs.BAG );
            }
            else
            {
                graph.add( associationURI, Rdfs.TYPE, Rdfs.CONTAINER );
            }
            if( manyAssociationType.rdf() != null )
            {
                graph.add( associationURI, Rdfs.SUB_PROPERTY_OF, values.createURI( manyAssociationType.rdf() ) );
            }

            URI associatedURI = values.createURI( manyAssociationType.uri() );
            graph.add( associationURI, Rdfs.RANGE, associatedURI );
            graph.add( associationURI, Rdfs.RANGE, XMLSchema.ANYURI );
        }
    }

    private void serializeAssociationTypes( final EntityType entityType,
                                            final Graph graph,
                                            final URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();
        // Associations
        for( AssociationType associationType : entityType.associations() )
        {
            URI associationURI = values.createURI( associationType.uri() );
            graph.add( associationURI, Rdfs.DOMAIN, entityTypeUri );
            graph.add( associationURI, Rdfs.TYPE, Rdfs.PROPERTY );
            if( associationType.rdf() != null )
            {
                graph.add( associationURI, Rdfs.SUB_PROPERTY_OF, values.createURI( associationType.rdf() ) );
            }

            URI associatedURI = values.createURI( Classes.toURI( associationType.type() ) );
            graph.add( associationURI, Rdfs.RANGE, associatedURI );
            graph.add( associationURI, Rdfs.RANGE, XMLSchema.ANYURI );
        }
    }

    private void serializePropertyTypes( final EntityType entityType,
                                         final Graph graph,
                                         final URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();

        // Properties
        for( PropertyType propertyType : entityType.properties() )
        {
            URI propertyURI = values.createURI( propertyType.uri() );
            graph.add( propertyURI, Rdfs.DOMAIN, entityTypeUri );
            graph.add( propertyURI, Rdfs.TYPE, Rdfs.PROPERTY );
            if( propertyType.rdf() != null )
            {
                graph.add( propertyURI, Rdfs.SUB_PROPERTY_OF, values.createURI( propertyType.rdf() ) );
            }

            // TODO Support more types
            if( propertyType.type() instanceof PrimitiveType )
            {
                URI type = dataTypes.get( ( (PrimitiveType) propertyType.type() ).type() );
                if( type != null )
                {
                    graph.add( propertyURI, Rdfs.RANGE, type );
                }
            }
        }
    }

    private void serializeQualifier( final Graph graph,
                                     final QualifierQualifiedIdentity arqi,
                                     final URI type,
                                     final Resource collection )
    {
        ValueFactory values = graph.getValueFactory();
        final BNode qualifier = values.createBNode();
        graph.add( collection, type, qualifier );
        graph.add( qualifier, RDF.TYPE, Qi4jRdf.TYPE_QUALIFIER );

        final URI assocEntityURI = values.createURI( arqi.toURI() );
        graph.add( qualifier, Qi4jEntity.ENTITY, assocEntityURI );
        final URI assocRoleURI = values.createURI( arqi.role().toURI() );
        graph.add( qualifier, Qi4jEntity.QUALIFIER, assocRoleURI );
    }
}
