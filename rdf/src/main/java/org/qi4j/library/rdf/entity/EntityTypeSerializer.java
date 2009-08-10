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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.XMLSchema;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rdf.Qi4jEntityType;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

/**
 * JAVADOC
 */
public class EntityTypeSerializer
{

    private Map<String, URI> dataTypes = new HashMap<String, URI>();

    public EntityTypeSerializer()
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

    public Iterable<Statement> serialize( final EntityType entityType )
    {
        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        URI entityTypeUri = values.createURI( entityType.uri() );

        graph.add( entityTypeUri, Rdfs.TYPE, Rdfs.CLASS );
        graph.add( entityTypeUri, Rdfs.TYPE, OWL.CLASS );

        graph.add(entityTypeUri, Qi4jEntityType.TYPE, values.createLiteral( entityType.type().toString() ));
        graph.add(entityTypeUri, Qi4jEntityType.QUERYABLE, values.createLiteral( entityType.queryable() ));

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

        // Mixin types
        for( String mixinType : entityType.mixinTypes() )
        {
//            if( !mixinType.equals( entityType.type() ) )
//            {
            graph.add( entityTypeUri, Rdfs.SUB_CLASS_OF, values.createURI( Classes.toURI( mixinType ) ) );
//            }
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
            URI associationURI = values.createURI( manyAssociationType.qualifiedName().toURI() );
            graph.add( associationURI, Rdfs.DOMAIN, entityTypeUri );

            graph.add( associationURI, Rdfs.TYPE, Rdfs.SEQ );

            URI associatedURI = values.createURI( manyAssociationType.qualifiedName().toURI() );
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
            URI associationURI = values.createURI( associationType.qualifiedName().toURI() );
            graph.add( associationURI, Rdfs.DOMAIN, entityTypeUri );
            graph.add( associationURI, Rdfs.TYPE, Rdfs.PROPERTY );

            URI associatedURI = values.createURI( associationType.type().toURI() );
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
            URI propertyURI = values.createURI( propertyType.qualifiedName().toURI() );
            graph.add( propertyURI, Rdfs.DOMAIN, entityTypeUri );
            graph.add( propertyURI, Rdfs.TYPE, Rdfs.PROPERTY );

            // TODO Support more types
            URI type = dataTypes.get( propertyType.type().type().name() );
            if( type != null )
            {
                graph.add( propertyURI, Rdfs.RANGE, type );
            }
        }
    }
}