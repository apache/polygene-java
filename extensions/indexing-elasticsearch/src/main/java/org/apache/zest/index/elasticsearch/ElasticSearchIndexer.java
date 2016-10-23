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
package org.apache.zest.index.elasticsearch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.qualifier.Tagged;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.time.SystemTime;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.api.value.ValueSerializer;
import org.apache.zest.api.value.ValueSerializer.Options;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entity.ManyAssociationState;
import org.apache.zest.spi.entity.NamedAssociationState;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.StateChangeListener;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen to Entity state changes and index them in ElasticSearch.
 *
 * QUID Use two indices, one for strict queries, one for full text and fuzzy search?
 */
@Mixins( ElasticSearchIndexer.Mixin.class )
public interface ElasticSearchIndexer
    extends StateChangeListener
{

    class Mixin
        implements StateChangeListener
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( ElasticSearchIndexer.class );

        @Structure
        private ModuleDescriptor module;

        @Service
        private EntityStore entityStore;

        @Service
        @Tagged( ValueSerialization.Formats.JSON )
        private ValueSerializer valueSerializer;

        @This
        private ElasticSearchSupport support;

        public void emptyIndex()
        {
            support.client().admin().indices().prepareDelete( support.index() ).execute().actionGet();
        }

        @Override
        public void notifyChanges( Iterable<EntityState> changedStates )
        {
            // All updated or new states
            Map<String, EntityState> newStates = new HashMap<>();
            for( EntityState eState : changedStates )
            {
                if( eState.status() == EntityStatus.UPDATED || eState.status() == EntityStatus.NEW )
                {
                    newStates.put( eState.entityReference().identity().toString(), eState );
                }
            }

            EntityStoreUnitOfWork uow = entityStore.newUnitOfWork(
                    module,
                    UsecaseBuilder.newUsecase( "Load associations for indexing" ),
                    SystemTime.now()
            );

            // Bulk index request builder
            BulkRequestBuilder bulkBuilder = support.client().prepareBulk();

            // Handle changed entity states
            for( EntityState changedState : changedStates )
            {
                if( changedState.entityDescriptor().queryable() )
                {
                    switch( changedState.status() )
                    {
                    case REMOVED:
                        LOGGER.trace( "Removing Entity State from Index: {}", changedState );
                        remove( bulkBuilder, changedState.entityReference().identity().toString() );
                        break;
                    case UPDATED:
                        LOGGER.trace( "Updating Entity State in Index: {}", changedState );
                        remove( bulkBuilder, changedState.entityReference().identity().toString() );
                        String updatedJson = toJSON( changedState, newStates, uow );
                        LOGGER.trace( "Will index: {}", updatedJson );
                        index( bulkBuilder, changedState.entityReference().identity().toString(), updatedJson );
                        break;
                    case NEW:
                        LOGGER.trace( "Creating Entity State in Index: {}", changedState );
                        String newJson = toJSON( changedState, newStates, uow );
                        LOGGER.trace( "Will index: {}", newJson );
                        index( bulkBuilder, changedState.entityReference().identity().toString(), newJson );
                        break;
                    case LOADED:
                    default:
                        // Ignored
                        break;
                    }
                }
            }

            uow.discard();

            if( bulkBuilder.numberOfActions() > 0 )
            {

                // Execute bulk actions
                BulkResponse bulkResponse = bulkBuilder.execute().actionGet();

                // Handle errors
                if( bulkResponse.hasFailures() )
                {
                    throw new ElasticSearchIndexException( bulkResponse.buildFailureMessage() );
                }

                LOGGER.debug( "Indexing changed Entity states took {}ms", bulkResponse.getTookInMillis() );

                // Refresh index
                support.client().admin().indices().prepareRefresh( support.index() ).execute().actionGet();
            }
        }

        private void remove( BulkRequestBuilder bulkBuilder, String identity )
        {
            bulkBuilder.add( support.client().
                prepareDelete( support.index(), support.entitiesType(), identity ) );
        }

        private void index( BulkRequestBuilder bulkBuilder, String identity, String json )
        {
            bulkBuilder.add( support.client().
                prepareIndex( support.index(), support.entitiesType(), identity ).
                setSource( json ) );
        }

        /**
         * <pre>
         * {
         *  "_identity": "ENTITY-IDENTITY",
         *  "_types": [ "All", "Entity", "types" ],
         *  "property.name": property.value,
         *  "association.name": { "reference": "ASSOCIATED-IDENTITY" }
         *  "manyassociation.name": [ { "reference": "ASSOCIATED" }, { "reference": "IDENTITIES" } ]
         *  "namedassociation.name": [ { "_named": "NAMED", "reference": "IDENTITY" } }
         * }
         * </pre>
         */
        private String toJSON( EntityState state, Map<String, EntityState> newStates, EntityStoreUnitOfWork uow )
        {
            JSONObject json = new JSONObject();

            try
            {
                json.put( "_identity", state.entityReference().identity().toString() );
                json.put( "_types", state.entityDescriptor()
                    .mixinTypes()
                    .map( Classes.toClassName() )
                    .collect( Collectors.toList() ) );
            }
            catch( JSONException e )
            {
                throw new ElasticSearchIndexException( "Could not index EntityState", e );
            }

            EntityDescriptor entityType = state.entityDescriptor();

            // Properties
            entityType.state().properties().forEach( propDesc -> {
                try
                {
                    if( propDesc.queryable() )
                    {
                        String key = propDesc.qualifiedName().name();
                        Object value = state.propertyValueOf( propDesc.qualifiedName() );
                        if( value == null || ValueType.isPrimitiveValue( value ) )
                        {
                            json.put( key, value );
                        }
                        else
                        {
                            String serialized = valueSerializer.serialize( new Options().withoutTypeInfo(), value );
                            // TODO Theses tests are pretty fragile, find a better way to fix this, Jackson API should behave better
                            if( serialized.startsWith( "{" ) )
                            {
                                json.put( key, new JSONObject( serialized ) );
                            }
                            else if( serialized.startsWith( "[" ) )
                            {
                                json.put( key, new JSONArray( serialized ) );
                            }
                            else
                            {
                                json.put( key, serialized );
                            }
                        }
                    }
                }
                catch( JSONException e )
                {
                    throw new ElasticSearchIndexException( "Could not index EntityState", e );
                }
            } );

            // Associations
            entityType.state().associations().forEach( assocDesc -> {
                try
                {
                    if( assocDesc.queryable() )
                    {
                        String key = assocDesc.qualifiedName().name();
                        EntityReference associated = state.associationValueOf( assocDesc.qualifiedName() );
                        Object value;
                        if( associated == null )
                        {
                            value = null;
                        }
                        else
                        {
                            if( assocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                if( newStates.containsKey( associated.identity().toString() ) )
                                {
                                    value = new JSONObject( toJSON( newStates.get( associated.identity().toString() ), newStates, uow ) );
                                }
                                else
                                {
                                    EntityReference reference = EntityReference.create( associated.identity() );
                                    EntityState assocState = uow.entityStateOf( module, reference );
                                    value = new JSONObject( toJSON( assocState, newStates, uow ) );
                                }
                            }
                            else
                            {
                                value = new JSONObject( Collections.singletonMap( "reference", associated.identity().toString() ) );
                            }
                        }
                        json.put( key, value );
                    }
                }
                catch( JSONException e )
                {
                    throw new ElasticSearchIndexException( "Could not index EntityState", e );
                }
            } );

            // ManyAssociations
            entityType.state().manyAssociations().forEach( manyAssocDesc -> {
                try
                {
                    if( manyAssocDesc.queryable() )
                    {
                        String key = manyAssocDesc.qualifiedName().name();
                        JSONArray array = new JSONArray();
                        ManyAssociationState associateds = state.manyAssociationValueOf( manyAssocDesc.qualifiedName() );
                        for( EntityReference associated : associateds )
                        {
                            if( manyAssocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                if( newStates.containsKey( associated.identity().toString() ) )
                                {
                                    array.put( new JSONObject( toJSON( newStates.get( associated.identity().toString() ), newStates, uow ) ) );
                                }
                                else
                                {
                                    EntityReference reference = EntityReference.create( associated.identity() );
                                    EntityState assocState = uow.entityStateOf( module, reference );
                                    array.put( new JSONObject( toJSON( assocState, newStates, uow ) ) );
                                }
                            }
                            else
                            {
                                array.put( new JSONObject( Collections.singletonMap( "reference", associated.identity().toString() ) ) );
                            }
                        }
                        json.put( key, array );
                    }
                }
                catch( JSONException e )
                {
                    throw new ElasticSearchIndexException( "Could not index EntityState", e );
                }
            } );

            // NamedAssociations
            entityType.state().namedAssociations().forEach( namedAssocDesc -> {
                try
                {
                    if( namedAssocDesc.queryable() )
                    {
                        String key = namedAssocDesc.qualifiedName().name();
                        JSONArray array = new JSONArray();
                        NamedAssociationState associateds = state.namedAssociationValueOf( namedAssocDesc.qualifiedName() );
                        for( String name : associateds )
                        {
                            Identity identity = associateds.get(name).identity();
                            if( namedAssocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                String identityString = identity.toString();
                                if( newStates.containsKey( identityString ) )
                                {
                                    JSONObject obj = new JSONObject( toJSON( newStates.get( identityString ), newStates, uow ) );
                                    obj.put( "_named", name );
                                    array.put( obj );
                                }
                                else
                                {
                                    EntityReference reference = EntityReference.create( identity );
                                    EntityState assocState = uow.entityStateOf( module, reference );
                                    JSONObject obj = new JSONObject( toJSON( assocState, newStates, uow ) );
                                    obj.put( "_named", name );
                                    array.put( obj );
                                }
                            }
                            else
                            {
                                JSONObject obj = new JSONObject();
                                obj.put( "_named", name );
                                obj.put( "reference", identity.toString() );
                                array.put( obj );
                            }
                        }
                        json.put( key, array );
                    }
                }
                catch( JSONException e )
                {
                    throw new ElasticSearchIndexException( "Could not index EntityState", e );
                }
            } );
            return json.toString();
        }
    }
}
