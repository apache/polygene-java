/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.library.rdf.entity;

import java.util.stream.Stream;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.service.qualifier.Tagged;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.api.value.ValueSerialization;
import org.apache.polygene.api.value.ValueSerializer;
import org.apache.polygene.api.value.ValueSerializer.Options;
import org.apache.polygene.library.rdf.Rdfs;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.ManyAssociationState;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;

/**
 * JAVADOC
 */
public class EntityStateSerializer
{

    @Service
    @Tagged( ValueSerialization.Formats.JSON )
    private ValueSerializer valueSerializer;

    public URI createEntityURI( ValueFactory valueFactory, EntityReference reference )
    {
        return valueFactory.createURI( reference.toURI() );
    }

    public Iterable<Statement> serialize( final EntityState entityState )
    {
        return serialize( entityState, true );
    }

    public Iterable<Statement> serialize( final EntityState entityState,
                                          final boolean includeNonQueryable
    )
    {
        Graph graph = new GraphImpl();
        serialize( entityState, includeNonQueryable, graph );
        return graph;
    }

    public void serialize( final EntityState entityState,
                           final boolean includeNonQueryable,
                           final Graph graph
    )
    {
        ValueFactory values = graph.getValueFactory();
        EntityReference reference = entityState.entityReference();
        URI entityUri = createEntityURI( values, reference );

        graph.add( entityUri,
                   Rdfs.TYPE,
                   values.createURI( Classes.toURI( entityState.entityDescriptor().types().findFirst().orElse( null ) ) ) );

        serializeProperties( entityState,
                             graph,
                             entityUri,
                             entityState.entityDescriptor(),
                             includeNonQueryable );

        serializeAssociations( entityState,
                               graph,
                               entityUri,
                               entityState.entityDescriptor().state().associations(),
                               includeNonQueryable );

        serializeManyAssociations( entityState,
                                   graph,
                                   entityUri,
                                   entityState.entityDescriptor().state().manyAssociations(),
                                   includeNonQueryable );
    }

    private void serializeProperties( final EntityState entityState,
                                      final Graph graph,
                                      final Resource subject,
                                      final EntityDescriptor entityType,
                                      final boolean includeNonQueryable
    )
    {
        // Properties
        entityType.state().properties().forEach( persistentProperty -> {
            Object property = entityState.propertyValueOf( persistentProperty.qualifiedName() );
            if( property != null )
            {
                serializeProperty( persistentProperty, property, subject, graph, includeNonQueryable );
            }
        } );
    }

    private void serializeProperty( PropertyDescriptor persistentProperty,
                                    Object property,
                                    Resource subject,
                                    Graph graph,
                                    boolean includeNonQueryable
    )
    {
        if( !( includeNonQueryable || persistentProperty.queryable() ) )
        {
            return; // Skip non-queryable
        }

        ValueType valueType = persistentProperty.valueType();

        final ValueFactory valueFactory = graph.getValueFactory();

        String propertyURI = persistentProperty.qualifiedName().toURI();
        URI predicate = valueFactory.createURI( propertyURI );
        String baseURI = propertyURI.substring( 0, propertyURI.indexOf( '#' ) ) + "/";

        if( valueType instanceof ValueCompositeType )
        {
            serializeValueComposite( subject, predicate, (ValueComposite) property, valueType,
                                     graph, baseURI, includeNonQueryable );
        }
        else
        {
            String stringProperty = valueSerializer.serialize( new Options().withoutTypeInfo(), property );
            final Literal object = valueFactory.createLiteral( stringProperty );
            graph.add( subject, predicate, object );
        }
    }

    private void serializeValueComposite( Resource subject, URI predicate,
                                          ValueComposite value,
                                          ValueType valueType,
                                          Graph graph,
                                          String baseUri,
                                          boolean includeNonQueryable
    )
    {
        final ValueFactory valueFactory = graph.getValueFactory();
        BNode collection = valueFactory.createBNode();
        graph.add( subject, predicate, collection );

        ( (ValueCompositeType) valueType ).properties().forEach( persistentProperty -> {
            Object propertyValue = PolygeneAPI.FUNCTION_COMPOSITE_INSTANCE_OF
                .apply( value )
                .state()
                .propertyFor( persistentProperty.accessor() )
                .get();

            if( propertyValue != null )
            {
                ValueType type = persistentProperty.valueType();
                if( type instanceof ValueCompositeType )
                {
                    URI pred = valueFactory.createURI( baseUri, persistentProperty.qualifiedName().name() );
                    serializeValueComposite( collection, pred, (ValueComposite) propertyValue, type, graph,
                                             baseUri + persistentProperty.qualifiedName().name() + "/",
                                             includeNonQueryable );
                }
                else
                {
                    serializeProperty( persistentProperty, propertyValue, collection, graph, includeNonQueryable );
                }
            }
        } );
    }

    private void serializeAssociations( final EntityState entityState,
                                        final Graph graph, URI entityUri,
                                        final Stream<? extends AssociationDescriptor> associations,
                                        final boolean includeNonQueryable
    )
    {
        ValueFactory values = graph.getValueFactory();

        // Associations
        associations.filter( type -> includeNonQueryable || type.queryable() ).forEach( associationType -> {
            EntityReference associatedId = entityState.associationValueOf( associationType.qualifiedName() );
            if( associatedId != null )
            {
                URI assocURI = values.createURI( associationType.qualifiedName().toURI() );
                URI assocEntityURI = values.createURI( associatedId.toURI() );
                graph.add( entityUri, assocURI, assocEntityURI );
            }
        } );
    }

    private void serializeManyAssociations( final EntityState entityState,
                                            final Graph graph,
                                            final URI entityUri,
                                            final Stream<? extends AssociationDescriptor> associations,
                                            final boolean includeNonQueryable
    )
    {
        ValueFactory values = graph.getValueFactory();

        // Many-Associations
        associations.filter( type -> includeNonQueryable || type.queryable() ).forEach( associationType -> {
            BNode collection = values.createBNode();
            graph.add( entityUri, values.createURI( associationType.qualifiedName().toURI() ), collection );
            graph.add( collection, Rdfs.TYPE, Rdfs.SEQ );

            ManyAssociationState associatedIds = entityState.manyAssociationValueOf( associationType.qualifiedName() );
            for( EntityReference associatedId : associatedIds )
            {
                URI assocEntityURI = values.createURI( associatedId.toURI() );
                graph.add( collection, Rdfs.LIST_ITEM, assocEntityURI );
            }
        } );
    }
}
