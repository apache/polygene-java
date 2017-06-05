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

package org.apache.polygene.index.rdf.indexing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.library.rdf.entity.EntityStateSerializer;
import org.apache.polygene.library.rdf.entity.EntityTypeSerializer;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.entitystore.StateChangeListener;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

@Mixins( RdfIndexerService.RdfEntityIndexerMixin.class )
@Activators( RdfIndexerService.Activator.class )
public interface RdfIndexerService extends StateChangeListener
{
    void initialize();

    File dataDir();

    class Activator extends ActivatorAdapter<ServiceReference<RdfIndexerService>>
    {

        @Override
        public void afterActivation( ServiceReference<RdfIndexerService> activated )
            throws Exception
        {
            activated.get().initialize();
        }
    }

    /**
     * JAVADOC Add JavaDoc
     */
    abstract class RdfEntityIndexerMixin
        implements RdfIndexerService
    {
        @Service
        private ServiceReference<Repository> repository;

        @Uses
        private EntityStateSerializer stateSerializer;

        @Uses
        private EntityTypeSerializer typeSerializer;

        private Set<EntityDescriptor> indexedEntityTypes;
        private ValueFactory valueFactory;

        @Override
        public void initialize()
        {
            indexedEntityTypes = new HashSet<>();
        }

        @Override
        public void notifyChanges( Iterable<EntityState> entityStates )
        {
            try
            {
                if( repository == null || !repository.isActive() ) // has been shut down, or not yet started...
                {
                    return;
                }
                final RepositoryConnection connection = repository.get().getConnection();
                // The Repository is being initialized and not ready yet.
                // This happens when the Repository is being initialized and it is accessing its own configuration.
                if( connection == null )
                {
                    return;
                }
                connection.setAutoCommit( false );
                try
                {
                    removeEntityStates( entityStates, connection );
                    connection.commit();
                    final Set<EntityDescriptor> entityTypes = indexUpdates( entityStates, connection );
                    indexNewTypes( connection, entityTypes );
                }
                finally
                {
                    connection.commit();
                    connection.close();
                }
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                //TODO What shall we do with the exception?
            }
        }

        private void indexNewTypes( RepositoryConnection connection, Set<EntityDescriptor> entityTypes )
            throws RepositoryException
        {
            // Index new types
            for( EntityDescriptor entityType : entityTypes )
            {
                if( !indexedEntityTypes.contains( entityType ) )
                {
                    indexEntityType( entityType, connection );
                    indexedEntityTypes.add( entityType );
                }
            }
        }

        private Set<EntityDescriptor> indexUpdates( Iterable<EntityState> entityStates,
                                                    RepositoryConnection connection
        )
            throws RepositoryException
        {
            // Figure out what to update
            final Set<EntityDescriptor> entityTypes = new HashSet<>();
            for( EntityState entityState : entityStates )
            {
                if( entityState.status().equals( EntityStatus.UPDATED ) )
                {
                    indexEntityState( entityState, connection );
                    entityTypes.add( entityState.entityDescriptor() );
                }
                else if( entityState.status().equals( EntityStatus.NEW ) )
                {
                    indexEntityState( entityState, connection );
                    entityTypes.add( entityState.entityDescriptor() );
                }
            }
            return entityTypes;
        }

        private void removeEntityStates( Iterable<EntityState> entityStates, RepositoryConnection connection )
            throws RepositoryException
        {
            List<URI> removedStates = new ArrayList<>();
            for( EntityState entityState : entityStates )
            {
                if( entityState.status().equals( EntityStatus.REMOVED ) )
                {
                    removedStates.add( stateSerializer.createEntityURI( getValueFactory(), entityState.entityReference() ) );
                }
                else if( entityState.status().equals( EntityStatus.UPDATED ) )
                {
                    removedStates.add( stateSerializer.createEntityURI( getValueFactory(), entityState.entityReference() ) );
                }
            }

            if( !removedStates.isEmpty() )
            {
                Resource[] resources = removedStates.toArray( new Resource[ removedStates.size() ] );
                connection.remove( null, null, null, resources );
            }
        }

        private void indexEntityState( final EntityState entityState,
                                       final RepositoryConnection connection
        )
            throws RepositoryException
        {
            if( entityState.entityDescriptor().queryable() )
            {
                EntityReference reference = entityState.entityReference();
                final URI entityURI = stateSerializer.createEntityURI( getValueFactory(), reference);
                Graph graph = new GraphImpl();
                stateSerializer.serialize( entityState, false, graph );
                connection.add( graph, entityURI );
            }
        }

        private void indexEntityType( final EntityDescriptor entityType,
                                      final RepositoryConnection connection
        )
            throws RepositoryException
        {
            if( entityType.queryable() )
            {
                String uri = Classes.toURI(entityType.types().findFirst().orElse(null));
                final URI compositeURI = getValueFactory().createURI( uri );
                // remove composite type if already present
                connection.clear( compositeURI );

                Iterable<Statement> statements = typeSerializer.serialize( entityType );
                connection.add( statements, compositeURI );
            }
        }

        private ValueFactory getValueFactory()
        {
            if( valueFactory == null )
            {
                valueFactory = repository.get().getValueFactory();
            }
            return valueFactory;
        }

        @Override
        public File dataDir()
        {
            return repository.get().getDataDir();
        }
    }
}
