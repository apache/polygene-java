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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.qi4j.entity.Identity;
import org.qi4j.entity.association.GenericAssociationInfo;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.property.GenericPropertyInfo;
import org.qi4j.spi.entity.AssociationType;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationType;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public class EntitySerializerMixin
    implements EntitySerializer
{
    private URI identityUri;
    private Map<String, URI> dataTypes = new HashMap<String, URI>();

    public EntitySerializerMixin()
    {
        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        identityUri = values.createURI( GenericPropertyInfo.toURI( Identity.class, "identity" ) );

        // TODO A ton more types need to be added here
        dataTypes.put( String.class.getName(), XMLSchema.STRING );
        dataTypes.put( Integer.class.getName(), XMLSchema.INT );
        dataTypes.put( Long.class.getName(), XMLSchema.LONG );
        dataTypes.put( Date.class.getName(), XMLSchema.DATETIME );
    }

    public Iterable<Statement> serialize( EntityState entityState )
    {
        QualifiedIdentity qid = entityState.qualifiedIdentity();
        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        URI entityUri = values.createURI( qid.toURI() );
        EntityType entityType = entityState.entityType();

        graph.add( entityUri, Rdfs.TYPE, values.createURI( entityType.toURI() ) );

        serializeProperties( entityState, graph, entityUri, entityType.properties() );
        serializeAssociations( entityState, graph, entityUri, entityType.associations() );
        serializeManyAssociations( entityState, graph, entityUri, entityType.manyAssociations() );

        return graph;
    }

    private void serializeProperties( EntityState entityState, Graph graph, URI entityUri, Iterable<PropertyType> properties )
    {
        ValueFactory values = graph.getValueFactory();

        // Properties
        for( PropertyType propertyType : properties )
        {
            Object value = entityState.getProperty( propertyType.qualifiedName() );
            if( value != null )
            {
                URI propertyUri = values.createURI( propertyType.uri() );
                Literal rdfValue = values.createLiteral( value.toString() );
                graph.add( entityUri, propertyUri, rdfValue );
            }
        }
    }

    private void serializeAssociations( EntityState entityState, Graph graph, URI entityUri, Iterable<AssociationType> associations )
    {
        ValueFactory values = graph.getValueFactory();

        // Associations
        for( AssociationType associationType : associations )
        {
            QualifiedIdentity associatedId = entityState.getAssociation( associationType.qualifiedName() );
            if( associatedId != null )
            {
                URI associationURI = values.createURI( associationType.uri() );
                URI associatedURI = values.createURI( associatedId.toURI() );
                graph.add( entityUri, associationURI, associatedURI );
            }
        }
    }

    private void serializeManyAssociations( EntityState entityState, Graph graph, URI entityUri, Iterable<ManyAssociationType> associations )
    {
        ValueFactory values = graph.getValueFactory();

        // Many-Associations
        for( ManyAssociationType associationType : associations )
        {
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
                URI associatedURI = values.createURI( associatedId.toURI() );
                graph.add( collection, Rdfs.LIST_ITEM, associatedURI );
            }
        }
    }

    public Iterable<Statement> serialize( EntityType entityType )
    {
        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        URI entityTypeUri = values.createURI( entityType.toURI() );

        graph.add( entityTypeUri, Rdfs.TYPE, Rdfs.CLASS );
        serializeMixinTypes( entityType, graph, entityTypeUri );


        serializePropertyTypes( entityType, graph, entityTypeUri );
        serializeAssociationTypes( entityType, graph, entityTypeUri );
        serializeManyAssociationTypes( entityType, graph, entityTypeUri );


        return graph;
    }

    private void serializeMixinTypes( EntityType entityType, Graph graph, URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();
        // Mixin types
        for( String mixinType : entityType.mixinTypes() )
        {
            if( !mixinType.equals( entityType.type() ) )
            {
                graph.add( entityTypeUri, Rdfs.SUB_CLASS_OF, values.createURI( ClassUtil.toURI( mixinType ) ) );
            }
        }
    }

    private void serializeManyAssociationTypes( EntityType entityType, Graph graph, URI entityTypeUri )
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

            URI associatedURI = values.createURI( manyAssociationType.uri() );
            graph.add( associationURI, Rdfs.RANGE, associatedURI );
            graph.add( associationURI, Rdfs.RANGE, XMLSchema.ANYURI );
        }
    }

    private void serializeAssociationTypes( EntityType entityType, Graph graph, URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();
        // Associations
        for( AssociationType associationType : entityType.associations() )
        {
            URI associationURI = values.createURI( GenericAssociationInfo.toURI( associationType.qualifiedName() ) );
            graph.add( associationURI, Rdfs.DOMAIN, entityTypeUri );
            graph.add( associationURI, Rdfs.TYPE, Rdfs.PROPERTY );
            URI associatedURI = values.createURI( ClassUtil.toURI( associationType.type() ) );
            graph.add( associationURI, Rdfs.DOMAIN, values.createURI( ClassUtil.toURI( associatedURI.getNamespace() ) ) );
            graph.add( associationURI, Rdfs.RANGE, associatedURI );
            graph.add( associationURI, Rdfs.RANGE, XMLSchema.ANYURI );
        }
    }

    private void serializePropertyTypes( EntityType entityType, Graph graph, URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();
        // Properties
        for( PropertyType propertyType : entityType.properties() )
        {
            URI propertyUri = values.createURI( propertyType.uri() );
            graph.add( propertyUri, Rdfs.DOMAIN, entityTypeUri );
            graph.add( propertyUri, Rdfs.TYPE, Rdfs.PROPERTY );

            URI type = dataTypes.get( propertyType.type() );
            if( type != null )
            {
                graph.add( propertyUri, Rdfs.RANGE, type );
            }
        }
    }
}
