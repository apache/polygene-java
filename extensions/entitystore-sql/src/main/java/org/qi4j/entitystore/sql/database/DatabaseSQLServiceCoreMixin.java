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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
public abstract class DatabaseSQLServiceCoreMixin
        implements DatabaseSQLService
{

    private static final Logger LOGGER = LoggerFactory.getLogger( DatabaseSQLServiceCoreMixin.class );

    @This
    private DatabaseSQLServiceState _state;

    @Structure
    private Application _application;

    @This
    protected DatabaseSQLServiceSpi _spi;

    @This
    private DatabaseSQLStringsBuilder _sqlStrings;

    public Connection getConnection()
            throws SQLException
    {
        return this._state.connection().get();
    }

    public void startDatabase()
            throws Exception
    {
        Connection connection = this._spi.createConnection();
        this._state.connection().set( connection );
        String schema = this._spi.getConfiguredSchemaName( connection );
        if ( schema == null ) {
            throw new EntityStoreException( "Schema name must not be null." );
        } else {
            this._state.schemaName().set( schema );

            if ( !this._spi.schemaExists( connection ) ) {
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    for ( String sql : this._sqlStrings.buildSQLForSchemaCreation() ) {
                        stmt.execute( sql );
                    }
                } finally {
                    SQLUtil.closeQuietly( stmt );
                }
                LOGGER.trace( "Schema {} created", schema );
            }

            if ( !this._spi.tableExists( connection ) ) {
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    for ( String sql : this._sqlStrings.buildSQLForTableCreation() ) {
                        stmt.execute( sql );
                    }
                    for ( String sql : this._sqlStrings.buildSQLForIndexCreation() ) {
                        stmt.execute( sql );
                    }
                } finally {
                    SQLUtil.closeQuietly( stmt );
                }
                LOGGER.trace( "Table {} created", SQLs.TABLE_NAME );
            }

            connection.setAutoCommit( false );

            this._state.pkLock().set( new Object() );
            synchronized ( this._state.pkLock().get() ) {
                this._state.nextEntityPK().set( this._spi.readNextEntityPK( connection ) );
            }

        }

    }

    public void stopDatabase()
            throws Exception
    {
        if ( Mode.production == this._application.mode() && getConnection() != null ) {
            SQLUtil.closeQuietly( getConnection() );
        }
    }

    public Long newPKForEntity()
    {
        if ( this._state.pkLock().get() == null || this._state.nextEntityPK().get() == null ) {
            throw new EntityStoreException( "New PK asked for entity, but database service has not been initialized properly." );
        }

        synchronized ( this._state.pkLock().get() ) {
            Long result = this._state.nextEntityPK().get();
            Long next = result + 1;
            this._state.nextEntityPK().set( next );

            return result;
        }
    }

}
