/*
 * Copyright 2012 Paul Merlin.
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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.json.JSONWriterSerializer;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateChangeListener;
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
        private Module module;

        @Service
        private EntityStore entityStore;

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
            Map<String, EntityState> newStates = new HashMap<String, EntityState>();
            for ( EntityState eState : changedStates ) {
                if ( eState.status() == EntityStatus.UPDATED || eState.status() == EntityStatus.NEW ) {
                    newStates.put( eState.identity().identity(), eState );
                }
            }

            EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( UsecaseBuilder.newUsecase( "Load associations for indexing" ),
                                                                   module,
                                                                   System.currentTimeMillis() );


            // Bulk index request builder
            BulkRequestBuilder bulkBuilder = support.client().prepareBulk();

            // Handle changed entity states
            for ( EntityState changedState : changedStates ) {
                if ( changedState.entityDescriptor().queryable() ) {
                    switch ( changedState.status() ) {
                        case REMOVED:
                            LOGGER.trace( "Removing Entity State from Index: {}", changedState );
                            remove( bulkBuilder, changedState.identity().identity() );
                            break;
                        case UPDATED:
                            LOGGER.trace( "Updating Entity State in Index: {}", changedState );
                            remove( bulkBuilder, changedState.identity().identity() );
                            index( bulkBuilder, changedState.identity().identity(), toJSON( changedState, newStates, uow ) );
                            break;
                        case NEW:
                            LOGGER.trace( "Creating Entity State in Index: {}", changedState );
                            index( bulkBuilder, changedState.identity().identity(), toJSON( changedState, newStates, uow ) );
                            break;
                        case LOADED:
                        default:
                            // Ignored
                            break;
                    }
                }
            }

            uow.discard();

            if ( bulkBuilder.numberOfActions() > 0 ) {

                // Execute bulk actions
                BulkResponse bulkResponse = bulkBuilder.execute().actionGet();

                // Handle errors
                if ( bulkResponse.hasFailures() ) {
                    throw new ElasticSearchIndexException( bulkResponse.buildFailureMessage() );
                }

                LOGGER.debug( "Indexing changed Entity states took {}ms", bulkResponse.tookInMillis() );

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
            LOGGER.trace( "Will index: {}", json );
            bulkBuilder.add( support.client().
                    prepareIndex( support.index(), support.entitiesType(), identity ).
                    setSource( json ) );
        }

        private String toJSON( EntityState state, Map<String, EntityState> newStates, EntityStoreUnitOfWork uow )
        {
            try {
                StringWriter writer = new StringWriter();
                JSONWriter json = new JSONWriter( writer );
                JSONWriter types = json.object().
                        key( "_identity" ).value( state.identity().identity() ).
                        key( "_types" ).array();
                for ( Class<?> type : state.entityDescriptor().mixinTypes() ) {
                    types.value( type.getName() );
                }
                types.endArray();
                EntityDescriptor entityType = state.entityDescriptor();
                JSONWriterSerializer serializer = new JSONWriterSerializer( json );

                // Properties
                for ( PropertyDescriptor persistentProperty : entityType.state().properties() ) {
                    if ( persistentProperty.queryable() ) {
                        Object value = state.getProperty( persistentProperty.qualifiedName() );
                        json.key( persistentProperty.qualifiedName().name() );
                        serializer.serialize( value, persistentProperty.valueType() );
                    }
                }

                // Associations
                for ( AssociationDescriptor assocDesc : entityType.state().associations() ) {
                    if ( assocDesc.queryable() ) {
                        EntityReference associated = state.getAssociation( assocDesc.qualifiedName() );
                        json.key( assocDesc.qualifiedName().name() );
                        if ( associated == null ) {
                            json.value( null );
                        } else {
                            if ( assocDesc.isAggregated() || support.indexNonAggregatedAssociations() ) {
                                if ( newStates.containsKey( associated.identity() ) ) {
                                    json.json( toJSON( newStates.get( associated.identity() ), newStates, uow ) );
                                } else {
                                    EntityState assocState = uow.getEntityState( EntityReference.parseEntityReference( associated.identity() ) );
                                    json.json( toJSON( assocState, newStates, uow ) );
                                }
                            } else {
                                json.object().key( "identity" ).value( associated.identity() ).endObject();
                            }
                        }
                    }
                }

                // ManyAssociations
                for ( AssociationDescriptor manyAssocDesc : entityType.state().manyAssociations() ) {
                    if ( manyAssocDesc.queryable() ) {
                        JSONWriter assocs = json.key( manyAssocDesc.qualifiedName().name() ).array();
                        ManyAssociationState associateds = state.getManyAssociation( manyAssocDesc.qualifiedName() );
                        for ( EntityReference associated : associateds ) {
                            if ( manyAssocDesc.isAggregated() || support.indexNonAggregatedAssociations() ) {
                                if ( newStates.containsKey( associated.identity() ) ) {
                                    assocs.json( toJSON( newStates.get( associated.identity() ), newStates, uow ) );
                                } else {
                                    EntityState assocState = uow.getEntityState( EntityReference.parseEntityReference( associated.identity() ) );
                                    assocs.json( toJSON( assocState, newStates, uow ) );
                                }
                            } else {
                                assocs.object().key( "identity" ).value( associated.identity() ).endObject();
                            }
                        }
                        assocs.endArray();
                    }
                }
                json.endObject();

                String result = writer.toString();
                return result;

            } catch ( JSONException e ) {
                throw new ElasticSearchIndexException( "Could not index EntityState", e );
            }

        }

    }

}
