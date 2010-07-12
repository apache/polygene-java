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
package org.qi4j.entitystore.sql.map;

import org.qi4j.entitystore.sql.map.AbstractDatabaseMapService;
import java.io.StringReader;
import java.io.Reader;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import static org.qi4j.entitystore.sql.database.SQLs.*;
import static org.qi4j.entitystore.sql.map.SQLESUtil.*;
import static org.qi4j.library.sql.common.SQLUtil.*;

public abstract class DatabaseDerbyMixin
        extends AbstractDatabaseMapService
{

    private static final long serialVersionUID = 1L;

    private static final String CREATE_MAP_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " (" + ENTITY_IDENTITY_COLUMN_NAME + " CHAR(128) PRIMARY KEY, " + ENTITY_STATE_COLUMN_NAME + " CLOB)";

    public DatabaseDerbyMixin( @This Configuration<DatabaseConfiguration> cfg )
    {
        super( cfg );
    }

    protected synchronized void initDatabase()
    {
        Connection connection = null;
        PreparedStatement createMapTable = null;
        try {
            connection = DriverManager.getConnection( buildConnectionString( cfg ) + ";create=true",
                                                      ensureConfiguration( cfg ).user().get(),
                                                      ensureConfiguration( cfg ).password().get() );
            connection.setAutoCommit( false );
            if ( needSchemaCreation( connection ) ) {
                createMapTable = connection.prepareStatement( CREATE_MAP_TABLE_SQL );
                createMapTable.executeUpdate();
                connection.commit();
                System.out.println( "Database successfully initialized" );
            } else {
                System.out.println( "Existing database found" );
            }
        } catch ( SQLException ex ) {
            throw new RuntimeException( "Unable to initialize database", ex );
        } finally {
            closeQuietly( createMapTable );
            closeQuietly( connection );
        }
    }

    @Override
    protected void shutdownDatabase()
            throws SQLException
    {
        DriverManager.getConnection( buildConnectionString( cfg ) + ";shudtown=true",
                                     ensureConfiguration( cfg ).user().get(),
                                     ensureConfiguration( cfg ).password().get() ).close();
    }

    public Reader getEntityValue( ResultSet resultSet )
            throws SQLException
    {
        return new StringReader( resultSet.getString( ENTITY_STATE_COLUMN_NAME ) );
    }

}
