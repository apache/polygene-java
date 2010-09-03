/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entitystore.sql.database;

import org.qi4j.entitystore.sql.datasource.DataSourceService;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.qi4j.api.injection.scope.Service;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Application.Mode;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.spi.entitystore.EntityStoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
@SuppressWarnings( "ProtectedField" )
public abstract class DatabaseSQLServiceCoreMixin
        implements DatabaseSQLService
{

    private static final Logger LOGGER = LoggerFactory.getLogger( DatabaseSQLServiceCoreMixin.class );

    @Structure
    private Application application;

    @Service
    private DataSourceService dataSourceService;

    @This
    private DatabaseSQLServiceState state;

    @This
    protected DatabaseSQLServiceSpi spi;

    @This
    private DatabaseSQLStringsBuilder sqlStrings;

    public Connection getConnection()
            throws SQLException
    {
        return dataSourceService.getDataSource().getConnection();
    }

    public void startDatabase()
            throws Exception
    {
        Connection connection = getConnection();
        String schema = dataSourceService.getConfiguredShemaName();
        if ( schema == null ) {
            throw new EntityStoreException( "Schema name must not be null." );
        } else {
            state.schemaName().set( schema );

            if ( !spi.schemaExists( connection ) ) {
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    for ( String sql : sqlStrings.buildSQLForSchemaCreation() ) {
                        stmt.execute( sql );
                    }
                } finally {
                    SQLUtil.closeQuietly( stmt );
                }
                LOGGER.trace( "Schema {} created", schema );
            }

            if ( !spi.tableExists( connection ) ) {
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    for ( String sql : sqlStrings.buildSQLForTableCreation() ) {
                        stmt.execute( sql );
                    }
                    for ( String sql : sqlStrings.buildSQLForIndexCreation() ) {
                        stmt.execute( sql );
                    }
                } finally {
                    SQLUtil.closeQuietly( stmt );
                }
                LOGGER.trace( "Table {} created", SQLs.TABLE_NAME );
            }

            connection.setAutoCommit( false );

            state.pkLock().set( new Object() );
            synchronized ( state.pkLock().get() ) {
                state.nextEntityPK().set( spi.readNextEntityPK( connection ) );
            }

        }

        SQLUtil.closeQuietly( connection );

    }

    public void stopDatabase()
            throws Exception
    {
        if ( Mode.production == application.mode() ) {
            // NOOP
        }
    }

    public Long newPKForEntity()
    {
        if ( state.pkLock().get() == null || state.nextEntityPK().get() == null ) {
            throw new EntityStoreException( "New PK asked for entity, but database service has not been initialized properly." );
        }
        synchronized ( state.pkLock().get() ) {
            Long result = state.nextEntityPK().get();
            Long next = result + 1;
            state.nextEntityPK().set( next );
            return result;
        }
    }

}
