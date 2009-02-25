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
import org.qi4j.api.common.QualifiedName;
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
import org.qi4j.spi.value.ValueState;
import org.qi4j.spi.value.ValueType;
import org.qi4j.spi.value.CollectionType;
import org.qi4j.spi.value.SerializableType;

/**
 * JAVADOC
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

        serializeProperties(
            entityType.properties(),
            new State()
            {
                public Object getProperty( final QualifiedName qualifiedName )
                {
                    return entityState.getProperty( qualifiedName );
                }
            },
            graph,
            entityUri,
            includeNonQueryable
        );
        serializeAssociations( entityState, graph, entityUri, entityType.associations(), includeNonQueryable );
        serializeManyAssociations( entityState, graph, entityUri, entityType.manyAssociations(), includeNonQueryable );
    }

    private void serializeProperties( final Iterable<PropertyType> propertyTypes,
                                      final State state,
                                      final Graph graph,
                                      final Resource subject,
                                      final boolean includeNonQueryable )
    {
        final ValueFactory valueFactory = graph.getValueFactory();

        // Properties
        for( PropertyType propertyType : propertyTypes )
        {
            if( !( includeNonQueryable || propertyType.queryable() ) )
            {
                continue; // Skip non-queryable
            }
            final Object property = state.getProperty( propertyType.qualifiedName() );
            if( property == null )
            {
                continue; // Skip properties with null values
            }
            final ValueType valueType = propertyType.type();
            if( valueType instanceof PrimitiveType )
            {
                final URI predicate = valueFactory.createURI( propertyType.qualifiedName().toURI() );
                final Literal object = valueFactory.createLiteral( property.toString() );
                graph.add( subject, predicate, object );
            }
            else if( valueType instanceof CompoundType )
            {
                // create a blank node
                final URI predicate = valueFactory.createURI( propertyType.qualifiedName().toURI() );
                final BNode object = valueFactory.createBNode();
                graph.add( subject, predicate, object );
                // serialize compound properties
                serializeProperties(
                    ( (CompoundType) valueType ).types(),
                    new State()
                    {
                        public Object getProperty( final QualifiedName qualifiedName )
                        {
                            return ( (ValueState) property ).getProperty( qualifiedName );
                        }
                    },
                    graph,
                    object,
                    includeNonQueryable
                );
            }
            else if( valueType instanceof CollectionType )
            {
                // TODO Support indexing of collection value types (Property<Collection<X>>)
            }
            else if( valueType instanceof SerializableType )
            {
                // TODO Support indexing of serializable value types (Property<X extends Serializable>)
            }
            else
            {
                throw new UnsupportedOperationException(
                    "RDF serialization of property type " + valueType.getClass().getName()
                    + " not supported (" + propertyType.qualifiedName() + ")"
                );
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
                URI assocURI = values.createURI( associationType.qualifiedName().toURI() );
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
            graph.add( entityUri, values.createURI( associationType.qualifiedName().toURI() ), collection );
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

    private interface State
    {
        Object getProperty( QualifiedName qualifiedName );
    }

}
