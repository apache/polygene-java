/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.index.rdf.indexing;

import org.openrdf.model.*;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.StateChangeListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixins( RdfIndexingService.RdfEntityIndexerMixin.class )
public interface RdfIndexingService
    extends StateChangeListener, Activatable
{
    File dataDir();

    /**
     * JAVADOC Add JavaDoc
     */
    class RdfEntityIndexerMixin
        implements RdfIndexingService, Activatable
    {
        @Service
        private Repository repository;

        @Uses
        private EntityStateSerializer stateSerializer;

        @Uses
        private EntityTypeSerializer typeSerializer;

        private Set<EntityDescriptor> indexedEntityTypes;
        private ValueFactory valueFactory;

        public void activate()
            throws Exception
        {
            indexedEntityTypes = new HashSet<EntityDescriptor>();
        }

        public void passivate()
            throws Exception
        {
        }

        public void notifyChanges( Iterable<EntityState> entityStates )
        {
            try
            {
                if( repository == null ) // has been shut down...
                {
                    return;
                }
                final RepositoryConnection connection = repository.getConnection();
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

                    // Figure out what to update
                    final Set<EntityDescriptor> entityTypes = new HashSet<EntityDescriptor>();
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

                    // Index new types
                    for( EntityDescriptor entityDescriptor : entityTypes )
                    {
                        if( !indexedEntityTypes.contains( entityDescriptor ) )
                        {
                            indexEntityType( entityDescriptor, connection );
                            indexedEntityTypes.add( entityDescriptor );
                        }
                    }
                }
                finally
                {
                    if( connection != null )
                    {
                        connection.commit();
                        connection.close();
                    }
                }
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                //TODO What shall we do with the exception?
            }
        }

        private void removeEntityStates( Iterable<EntityState> entityStates, RepositoryConnection connection )
            throws RepositoryException
        {
            List<URI> removedStates = new ArrayList<URI>();
            for( EntityState entityState : entityStates )
            {
                if( entityState.status().equals( EntityStatus.REMOVED ) )
                {
                    removedStates.add( stateSerializer.createEntityURI( getValueFactory(), entityState.identity() ) );
                }
                else if( entityState.status().equals( EntityStatus.UPDATED ) )
                {
                    removedStates.add( stateSerializer.createEntityURI( getValueFactory(), entityState.identity() ) );
                }
            }

            if( !removedStates.isEmpty() )
            {
                Resource[] resources = removedStates.toArray( new Resource[removedStates.size()] );
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
                final URI entityURI = stateSerializer.createEntityURI( getValueFactory(), entityState.identity() );
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
                final URI compositeURI = getValueFactory().createURI( Classes.toURI(entityType.type()) );
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
                valueFactory = repository.getValueFactory();
            }
            return valueFactory;
        }

        public File dataDir()
        {
            return repository.getDataDir();
        }
    }
}
