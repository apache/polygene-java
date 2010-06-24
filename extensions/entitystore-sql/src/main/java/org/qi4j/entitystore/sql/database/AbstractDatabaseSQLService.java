/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Application.Mode;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.spi.entitystore.EntityStoreException;

/**
 *
 * @author Stanislav Muhametsin
 */
public abstract class AbstractDatabaseSQLService implements DatabaseSQLService
{

    @This private DatabaseSQLServiceState _state;

    @Structure private Application _application;

    public Connection getConnection()
        throws SQLException
    {
        return this._state.connection().get();
    }

    public void startDatabase()
        throws Exception
    {
        Connection connection = this.createConnection();
        this._state.connection().set( connection );
        String schema = this.getSchemaName( connection );
        if (schema != null)
        {
            this._state.schemaName().set( schema );

            if (!this.schemaExists( connection ))
            {
                Statement stmt = null;
                try
                {
                    stmt = connection.createStatement();
                    for (String sql : this.getSQLForSchemaCreation())
                    {
                        stmt.execute( sql );
                    }
                } finally
                {
                    SQLUtil.closeQuietly( stmt );
                }
            }

            if (!this.tableExists( connection ))
            {
                Statement stmt = null;
                try
                {
                    stmt = connection.createStatement();
                    for (String sql : this.getSQLForTableCreation())
                    {
                        stmt.execute( sql );
                    }
                    for (String sql : this.getSQLForIndexCreation())
                    {
                        stmt.execute( sql );
                    }
                } finally
                {
                    SQLUtil.closeQuietly( stmt );
                }
            }

            connection.setAutoCommit( false );

            this._state.pkLock().set( new Object() );
            synchronized(this._state.pkLock().get())
            {
                this._state.nextEntityPK().set( this.readNextEntityPK(connection) );
            }


        } else
        {
            throw new EntityStoreException( "Schema name must not be null." );
        }

    }

    public void stopDatabase()
        throws Exception
    {
        if (Mode.production.equals( this._application.mode()) && this._state.connection().get() != null)
        {
            SQLUtil.closeQuietly( this._state.connection().get() );
        }
    }

    public PreparedStatement prepareGetAllEntitiesStatement( Connection connection )
        throws SQLException
    {
        return connection.prepareStatement( this.getSQLForSelectAllEntitiesStatement() );
    }

    public PreparedStatement prepareGetEntityStatement( Connection connection )
        throws SQLException
    {
        return connection.prepareStatement( this.getSQLForSelectEntityStatement() );
    }

    public PreparedStatement prepareInsertEntityStatement( Connection connection )
        throws SQLException
    {
        return connection.prepareStatement( this.getSQLForInsertEntityStatement() );
    }

    public PreparedStatement prepareRemoveEntityStatement( Connection connection )
        throws SQLException
    {
        return connection.prepareStatement( this.getSQLForRemoveEntityStatement() );
    }

    public PreparedStatement prepareUpdateEntityStatement( Connection connection )
        throws SQLException
    {
        return connection.prepareStatement( this.getSQLForUpdateEntityStatement() );
    }

    public Long newPKForEntity()
    {
        if (this._state.pkLock().get() == null || this._state.nextEntityPK().get() == null)
        {
            throw new EntityStoreException( "New PK asked for entity, but database service has not been initialized properly." );
        }

        synchronized(this._state.pkLock().get())
        {
            Long result = this._state.nextEntityPK().get();
            Long next = result + 1;
            this._state.nextEntityPK().set( next );

            return result;
        }
    }

    protected String getSchemaName()
    {
        return this._state.schemaName().get();
    }

    protected abstract String getSQLForSelectAllEntitiesStatement();

    protected abstract String getSQLForSelectEntityStatement();

    protected abstract String getSQLForInsertEntityStatement();

    protected abstract String getSQLForRemoveEntityStatement();

    protected abstract String getSQLForUpdateEntityStatement();

    protected abstract Connection createConnection() throws SQLException;

    protected abstract String getSchemaName(Connection connection) throws SQLException;

    protected abstract boolean schemaExists(Connection connection) throws SQLException;

    protected abstract boolean tableExists(Connection connection) throws SQLException;

    protected abstract long readNextEntityPK(Connection connection) throws SQLException;

    protected abstract String[] getSQLForSchemaCreation();

    protected abstract String[] getSQLForTableCreation();

    protected abstract String[] getSQLForIndexCreation();

}
