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

import java.sql.Connection;
import java.sql.SQLException;

import org.qi4j.api.configuration.Configuration;
import static org.qi4j.entitystore.sql.database.SQLs.*;
import org.qi4j.entitystore.sql.map.DatabaseConfiguration;

public class SQLESUtil
{

    public static final String SELECT_ALL_STATES_SQL = "SELECT " + ENTITY_STATE_COLUMN_NAME + " FROM " + TABLE_NAME;

    public static final String SELECT_STATE_SQL = SELECT_ALL_STATES_SQL + " WHERE " + ENTITY_IDENTITY_COLUMN_NAME + "=?";

    public static final String INSERT_STATE_SQL = "INSERT INTO " + TABLE_NAME + " (" + ENTITY_IDENTITY_COLUMN_NAME + ", " + ENTITY_STATE_COLUMN_NAME + ") VALUES (?, ?)";

    public static final String UPDATE_STATE_SQL = "UPDATE " + TABLE_NAME + " SET " + ENTITY_STATE_COLUMN_NAME + "=? WHERE " + ENTITY_IDENTITY_COLUMN_NAME + "=?";

    public static final String REMOVE_STATE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + ENTITY_IDENTITY_COLUMN_NAME + "=?";

    public static DatabaseConfiguration ensureConfiguration( Configuration<DatabaseConfiguration> cfg )
    {
        DatabaseConfiguration config = cfg.configuration();
        if ( config == null ) {
            throw new RuntimeException( "DatabaseService is not properly configured" );
        }
        return config;
    }

    public static String buildConnectionString( Configuration<DatabaseConfiguration> cfg )
    {
        DatabaseConfiguration config = ensureConfiguration( cfg );
        return config.connectionURL().get() + config.dbName().get();
    }

    public static boolean needSchemaCreation( Connection connection )
            throws SQLException
    {
        return !connection.getMetaData().getTables( null, null, TABLE_NAME, null ).next()
                && !connection.getMetaData().getTables( null, null, TABLE_NAME.toLowerCase(), null ).next();
    }

    private SQLESUtil()
    {
    }

}
