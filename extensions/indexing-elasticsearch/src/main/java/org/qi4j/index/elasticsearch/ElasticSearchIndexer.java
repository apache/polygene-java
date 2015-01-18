/*
 * Copyright 2012-2014 Paul Merlin.
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
package org.qi4j.index.elasticsearch;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializer;
import org.qi4j.api.value.ValueSerializer.Options;
import org.qi4j.functional.Iterables;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialIndexer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.NamedAssociationState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        private Module module;
        @Service
        private EntityStore entityStore;
        @Service
        @Tagged( ValueSerialization.Formats.JSON )
        private ValueSerializer valueSerializer;
        @This
        private ElasticSearchSupport support;

        Stack<String> deepSpatialMappingKeyStack = new Stack<>();


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
                    newStates.put( eState.identity().identity(), eState );
                }
            }

            EntityStoreUnitOfWork uow = entityStore.newUnitOfWork(
                UsecaseBuilder.newUsecase( "Load associations for indexing" ),
                module,
                System.currentTimeMillis()
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
                            remove( bulkBuilder, changedState.identity().identity() );
                            break;
                        case UPDATED:
                            LOGGER.trace( "Updating Entity State in Index: {}", changedState );
                            remove( bulkBuilder, changedState.identity().identity() );
                            String updatedJson = toJSON(changedState, newStates, uow );
                            LOGGER.trace( "Will index: {}", updatedJson );
                            index( bulkBuilder, changedState.identity().identity(), updatedJson );
                            break;
                        case NEW:
                            LOGGER.trace( "Creating Entity State in Index: {}", changedState );
                            String newJson = toJSON(changedState, newStates, uow );
                            LOGGER.trace( "Will index: {}", newJson );
                            index( bulkBuilder, changedState.identity().identity(), newJson );
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
         *  "association.name": { "identity": "ASSOCIATED-IDENTITY" }
         *  "manyassociation.name": [ { "identity": "ASSOCIATED" }, { "identity": "IDENTITIES" } ]
         *  "namedassociation.name": [ { "_named": "NAMED", "identity": "IDENTITY" } }
         * }
         * </pre>
         */


        private String toJSON(EntityState state, Map<String, EntityState> newStates, EntityStoreUnitOfWork uow )
        {
            return toJSON(null, state, newStates, uow);
        }
        private String toJSON(String mKey, EntityState state, Map<String, EntityState> newStates, EntityStoreUnitOfWork uow )
        {

            try
            {
                JSONObject json = new JSONObject();

                json.put( "_identity", state.identity().identity() );
                json.put( "_types", Iterables.toList( Iterables.map( Classes.toClassName(), state.entityDescriptor().mixinTypes() ) ) );

                EntityDescriptor entityType = state.entityDescriptor();

                // Properties
                for( PropertyDescriptor propDesc : entityType.state().properties() )
                {

                    if( propDesc.queryable() )
                    {
                        String key = propDesc.qualifiedName().name();
                        Object value = state.propertyValueOf( propDesc.qualifiedName() );
                        if( value == null || ValueType.isPrimitiveValue( value ) )
                        {
                            json.put(key, value);
                        }
                        else
                        // For spatial types is is required to generate a "deep mapping key" that is used during indexing as
                        // mapping name. The mapping name has to be "deep" and is also used for spatial queries :
                        //    "geo_distance" : {
                        //     "city.location" : [ 11.57958984375, 48.13905780942574 ],
                        //     "distance" : "10.0km"
                        if (ValueType.isGeometricValue(value) )
                        {
                            if (mKey != null)
                            {
                                deepSpatialMappingKeyStack.add(mKey);
                                deepSpatialMappingKeyStack.add(key);
                            }
                            else { deepSpatialMappingKeyStack.add(key);}

                            String deepKey = ElasticSearchSpatialIndexer.spatialMappingPropertyName(deepSpatialMappingKeyStack);
                            ElasticSearchSpatialIndexer.toJSON(support, (TGeometry) value, key, deepKey, json, module);
                            deepSpatialMappingKeyStack.clear();
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

                // Associations
                for( AssociationDescriptor assocDesc : entityType.state().associations() )
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
                                if( newStates.containsKey( associated.identity() ) )
                                {
                                    value = new JSONObject( toJSON(key, newStates.get( associated.identity() ), newStates, uow ) );
                                }
                                else
                                {
                                    EntityState assocState = uow.entityStateOf( EntityReference.parseEntityReference( associated.identity() ) );
                                    value = new JSONObject( toJSON(key, assocState, newStates, uow ) );
                                }
                            }
                            else
                            {
                                value = new JSONObject( Collections.singletonMap( "identity", associated.identity() ) );
                            }
                        }
                        json.put( key, value );
                    }
                }

                // ManyAssociations
                for( AssociationDescriptor manyAssocDesc : entityType.state().manyAssociations() )
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
                                if( newStates.containsKey( associated.identity() ) )
                                {
                                    array.put( new JSONObject( toJSON(key, newStates.get( associated.identity() ), newStates, uow ) ) );
                                }
                                else
                                {
                                    EntityState assocState = uow.entityStateOf( EntityReference.parseEntityReference( associated.identity() ) );
                                    array.put( new JSONObject( toJSON(key, assocState, newStates, uow ) ) );
                                }
                            }
                            else
                            {
                                array.put( new JSONObject( Collections.singletonMap( "identity", associated.identity() ) ) );
                            }
                        }
                        json.put( key, array );
                    }
                }

                // NamedAssociations
                for( AssociationDescriptor namedAssocDesc : entityType.state().namedAssociations() )
                {
                    if( namedAssocDesc.queryable() )
                    {
                        String key = namedAssocDesc.qualifiedName().name();
                        JSONArray array = new JSONArray();
                        NamedAssociationState associateds = state.namedAssociationValueOf( namedAssocDesc.qualifiedName() );
                        for( String name : associateds )
                        {
                            if( namedAssocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                String identity = associateds.get( name ).identity();
                                if( newStates.containsKey( identity ) )
                                {
                                    JSONObject obj = new JSONObject( toJSON(key, newStates.get( identity ), newStates, uow ) );
                                    obj.put( "_named", name );
                                    array.put( obj );
                                }
                                else
                                {
                                    EntityState assocState = uow.entityStateOf( EntityReference.parseEntityReference( identity ) );
                                    JSONObject obj = new JSONObject( toJSON(key, assocState, newStates, uow ) );
                                    obj.put( "_named", name );
                                    array.put( obj );
                                }
                            }
                            else
                            {
                                JSONObject obj = new JSONObject();
                                obj.put( "_named", name );
                                obj.put( "identity", associateds.get( name ).identity() );
                                array.put( obj );
                            }
                        }
                        json.put( key, array );
                    }
                }
                return json.toString();
            }
            catch( JSONException e )
            {
                throw new ElasticSearchIndexException( "Could not index EntityState", e );
            }
        }
    }

}
