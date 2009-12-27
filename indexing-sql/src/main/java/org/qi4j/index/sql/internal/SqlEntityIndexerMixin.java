/*
 * Copyright 2009 Niclas Hedhman
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.index.sql.IndexingConfiguration;
import org.qi4j.library.jdbc.ConnectionPool;
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

    @This
    private Configuration<IndexingConfiguration> config;

    public void activate()
        throws SQLException
    {
        IndexingConfiguration conf = config.configuration();
        Connection conn = connectionPool.obtainConnection();
        try
        {
            Statement st = conn.createStatement();
            checkAndCreateAssociationsTable( conf, st );

            checkAndCreateValuesTable( conf, st );

            checkAndCreatePropertiesTable( conf, st );
            conn.commit();
        }
        finally
        {
            connectionPool.releaseConnection( conn );
        }
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


    private void checkAndCreateAssociationsTable( IndexingConfiguration configuration, Statement st )
        throws SQLException
    {
        try
        {
            String checkStatement = configuration.checkAssociationsTable().get();
            if( checkStatement == null )
            {
                checkStatement = "SELECT * FROM QI_ASSOCIATIONS";
            }
            st.execute( checkStatement );
        }
        catch( SQLException e )
        {
            String createStatement = configuration.createAssociationsTable().get();
            if( createStatement == null )
            {
                createStatement = "CREATE TABLE QI_ASSOCIATIONS ENTITY_ID varchar(130), ASSOC_NAME varchar(250), REF_ID varchar(250)";
            }
            st.execute( createStatement );
        }
    }

    private void checkAndCreatePropertiesTable( IndexingConfiguration configuration, Statement st )
        throws SQLException
    {
        try
        {
            String checkStatement = configuration.checkPropertiesTable().get();
            if( checkStatement == null )
            {
                checkStatement = "SELECT * FROM QI_PROPERTIES";
            }
            st.execute( checkStatement );
        }
        catch( SQLException e )
        {
            String createStatement = configuration.createPropertiesTable().get();
            if( createStatement == null )
            {
                createStatement = "CREATE TABLE QI_PROPERTIES PROPERTY_ID varchar(130), PROPERTY_NAME varchar(250), PROPERTY_TYPE varchar(250), PROPERTY_DATA TEXT";
            }
            st.execute( createStatement );
        }
    }

    private void checkAndCreateValuesTable( IndexingConfiguration configuration, Statement st )
        throws SQLException
    {
        try
        {
            String checkStatement = configuration.checkValuesTable().get();
            if( checkStatement == null )
            {
                checkStatement = "SELECT * FROM QI_VALUES";
            }
            st.execute( checkStatement );
        }
        catch( SQLException e )
        {
            String createStatement = configuration.createValuesTable().get();
            if( createStatement == null )
            {
                createStatement = "CREATE TABLE QI_VALUES VALUE_ID varchar(130), VALUE_TYPE varchar(250), VALUE_DATA TEXT";
            }
            st.execute( createStatement );
        }
    }

}
