/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.index.sql.internal;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.Activatable;
import org.qi4j.index.sql.jdbc.ConnectionPool;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.StateChangeListener;

/**
 * JAVADOC Add JavaDoc
 */
public class SqlEntityIndexerMixin
    implements StateChangeListener, Activatable
{
    @Service
    private ConnectionPool connectionPool;

    private Set<EntityType> indexedEntityTypes;

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
            final Connection connection = connectionPool.obtainConnection();
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
                                   final Connection connection
    )
    {
    }

    private void removeEntityState( final EntityReference identity,
                                    final Connection connection
    )
    {
    }

    private void indexEntityType( final EntityType entityType,
                                  final Connection connection
    )
    {
    }
}
