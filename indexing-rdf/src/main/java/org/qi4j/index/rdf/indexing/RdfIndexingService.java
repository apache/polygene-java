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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.StateChangeListener;

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

        private Set<EntityType> indexedEntityTypes;
        private ValueFactory valueFactory;

        public void activate()
            throws Exception
        {
            indexedEntityTypes = new HashSet<EntityType>();
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
                    // Figure out what to update
                    final Set<EntityType> entityTypes = new HashSet<EntityType>();
                    for( EntityState entityState : entityStates )
                    {
                        if( entityState.status().equals( EntityStatus.REMOVED ) )
                        {
                            removeEntityState( entityState.identity(), connection );
                        }
                        else if( entityState.status().equals( EntityStatus.UPDATED ) )
                        {
                            removeEntityState( entityState.identity(), connection );
                            indexEntityState( entityState, connection );
                            entityTypes.add( entityState.entityDescriptor().entityType() );
                        }
                        else if( entityState.status().equals( EntityStatus.NEW ) )
                        {
                            indexEntityState( entityState, connection );
                            entityTypes.add( entityState.entityDescriptor().entityType() );
                        }
                    }

                    // Index new types
                    for( EntityType entityType : entityTypes )
                    {
                        if( !indexedEntityTypes.contains( entityType ) )
                        {
                            indexEntityType( entityType, connection );
                            indexedEntityTypes.add( entityType );
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

        private void indexEntityState( final EntityState entityState,
                                       final RepositoryConnection connection
        )
            throws RepositoryException
        {
            final URI entityURI = stateSerializer.createEntityURI( getValueFactory(), entityState.identity() );
            Graph graph = new GraphImpl();
            stateSerializer.serialize( entityState, false, graph );
            connection.add( graph, entityURI );
        }

        private void removeEntityState( final EntityReference identity,
                                        final RepositoryConnection connection
        )
            throws RepositoryException
        {
            connection.clear( stateSerializer.createEntityURI( getValueFactory(), identity ) );
        }

        private void indexEntityType( final EntityType entityType,
                                      final RepositoryConnection connection
        )
            throws RepositoryException
        {
            final URI compositeURI = getValueFactory().createURI( entityType.uri() );
            // remove composite type if already present
            connection.clear( compositeURI );

            Iterable<Statement> statements = typeSerializer.serialize( entityType );
            connection.add( statements, compositeURI );
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
