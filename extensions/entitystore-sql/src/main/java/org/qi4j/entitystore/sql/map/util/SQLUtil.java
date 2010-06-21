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
package org.qi4j.entitystore.sql.map.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.entitystore.sql.map.database.DatabaseConfiguration;

public class SQLUtil
{

    public static final String TABLE_NAME = "QI4J_DATA";

    public static final String IDENTITY_COLUMN = "QI_IDENTITY";

    public static final String STATE_COLUMN = "QI_STATE";

    public static final String SELECT_ALL_STATES_SQL = "SELECT " + STATE_COLUMN + " FROM " + TABLE_NAME;

    public static final String SELECT_STATE_SQL = "SELECT " + STATE_COLUMN + " FROM " + TABLE_NAME + " WHERE " + IDENTITY_COLUMN + "=?";

    public static final String INSERT_STATE_SQL = "INSERT INTO " + TABLE_NAME + " (" + IDENTITY_COLUMN + ", " + STATE_COLUMN + ") VALUES (?, ?)";

    public static final String UPDATE_STATE_SQL = "UPDATE " + TABLE_NAME + " SET " + STATE_COLUMN + "=? WHERE " + IDENTITY_COLUMN + "=?";

    public static final String REMOVE_STATE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + IDENTITY_COLUMN + "=?";

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

    public static void closeQuietly( ResultSet resultSet )
    {
        if ( resultSet != null ) {
            try {
                resultSet.close();
            } catch ( SQLException ignored ) {
            }
        }
    }

    public static void closeQuietly( Statement select )
    {
        if ( select != null ) {
            try {
                select.close();
            } catch ( SQLException ignored ) {
            }
        }
    }

    public static void closeQuietly( Connection connection )
    {
        if ( connection != null ) {
            try {
                connection.close();
            } catch ( SQLException ignored ) {
            }
        }
    }

    public static void rollbackQuietly( Connection connection )
    {
        if ( connection != null ) {
            try {
                connection.rollback();
            } catch ( SQLException ignored ) {
            }
        }
    }

    private SQLUtil()
    {
    }

}
