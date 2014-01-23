/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.rdf.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.XMLSchema;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rdf.Qi4jEntityType;
import org.qi4j.library.rdf.Rdfs;

import static org.qi4j.functional.Iterables.first;

/**
 * JAVADOC
 */
public class EntityTypeSerializer
{

    private final Map<String, URI> dataTypes = new HashMap<>( 12 );

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
        dataTypes.put( DateTime.class.getName(), XMLSchema.DATETIME );
        dataTypes.put( LocalDateTime.class.getName(), XMLSchema.DATETIME );
        dataTypes.put( LocalDate.class.getName(), XMLSchema.DATE );
    }

    public Iterable<Statement> serialize( final EntityDescriptor entityDescriptor )
    {
        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        URI entityTypeUri = values.createURI( Classes.toURI( first( entityDescriptor.types() ) ) );

        graph.add( entityTypeUri, Rdfs.TYPE, Rdfs.CLASS );
        graph.add( entityTypeUri, Rdfs.TYPE, OWL.CLASS );

        graph.add( entityTypeUri, Qi4jEntityType.TYPE, values.createLiteral( first( entityDescriptor.types() ).toString() ) );
        graph.add( entityTypeUri, Qi4jEntityType.QUERYABLE, values.createLiteral( entityDescriptor.queryable() ) );

        serializeMixinTypes( entityDescriptor, graph, entityTypeUri );

        serializePropertyTypes( entityDescriptor, graph, entityTypeUri );
        serializeAssociationTypes( entityDescriptor, graph, entityTypeUri );
        serializeManyAssociationTypes( entityDescriptor, graph, entityTypeUri );

        return graph;
    }

    private void serializeMixinTypes( final EntityDescriptor entityDescriptor,
                                      final Graph graph,
                                      final URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();

        // Mixin types
        for( Class<?> mixinType : entityDescriptor.mixinTypes() )
        {
            graph.add( entityTypeUri, Rdfs.SUB_CLASS_OF, values.createURI( Classes.toURI( mixinType ) ) );
        }
    }

    private void serializeManyAssociationTypes( final EntityDescriptor entityDescriptor,
                                                final Graph graph,
                                                final URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();
        // ManyAssociations
        for( AssociationDescriptor manyAssociationType : entityDescriptor.state().manyAssociations() )
        {
            URI associationURI = values.createURI( manyAssociationType.qualifiedName().toURI() );
            graph.add( associationURI, Rdfs.DOMAIN, entityTypeUri );

            graph.add( associationURI, Rdfs.TYPE, Rdfs.SEQ );

            URI associatedURI = values.createURI( manyAssociationType.qualifiedName().toURI() );
            graph.add( associationURI, Rdfs.RANGE, associatedURI );
            graph.add( associationURI, Rdfs.RANGE, XMLSchema.ANYURI );
        }
    }

    private void serializeAssociationTypes( final EntityDescriptor entityDescriptor,
                                            final Graph graph,
                                            final URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();
        // Associations
        for( AssociationDescriptor associationType : entityDescriptor.state().associations() )
        {
            URI associationURI = values.createURI( associationType.qualifiedName().toURI() );
            graph.add( associationURI, Rdfs.DOMAIN, entityTypeUri );
            graph.add( associationURI, Rdfs.TYPE, Rdfs.PROPERTY );

            URI associatedURI = values.createURI( Classes.toURI( Classes.RAW_CLASS.map( associationType.type() ) ) );
            graph.add( associationURI, Rdfs.RANGE, associatedURI );
            graph.add( associationURI, Rdfs.RANGE, XMLSchema.ANYURI );
        }
    }

    private void serializePropertyTypes( final EntityDescriptor entityDescriptor,
                                         final Graph graph,
                                         final URI entityTypeUri )
    {
        ValueFactory values = graph.getValueFactory();

        // Properties
        for( PropertyDescriptor persistentProperty : entityDescriptor.state().properties() )
        {
            URI propertyURI = values.createURI( persistentProperty.qualifiedName().toURI() );
            graph.add( propertyURI, Rdfs.DOMAIN, entityTypeUri );
            graph.add( propertyURI, Rdfs.TYPE, Rdfs.PROPERTY );

            // TODO Support more types
            URI type = dataTypes.get( persistentProperty.valueType().mainType().getName() );
            if( type != null )
            {
                graph.add( propertyURI, Rdfs.RANGE, type );
            }
        }
    }
}
