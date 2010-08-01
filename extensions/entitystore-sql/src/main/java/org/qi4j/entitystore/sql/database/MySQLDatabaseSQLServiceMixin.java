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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.qi4j.api.injection.scope.This;
import static org.qi4j.entitystore.sql.database.SQLs.*;
import org.qi4j.library.sql.common.SQLUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public abstract class MySQLDatabaseSQLServiceMixin
        implements DatabaseSQLService, DatabaseSQLStringsBuilder, DatabaseSQLServiceSpi
{

    private static final Logger LOGGER = LoggerFactory.getLogger( MySQLDatabaseSQLServiceMixin.class );

    private static final String CREATE_TABLE_SQL = "CREATE TABLE %s." + TABLE_NAME + " ("
            + ENTITY_PK_COLUMN_NAME + " BIGINT PRIMARY KEY, "
            + ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME + " BIGINT NOT NULL, "
            + ENTITY_IDENTITY_COLUMN_NAME + " VARCHAR(256) NOT NULL UNIQUE, "
            + ENTITY_STATE_COLUMN_NAME + " LONGTEXT NOT NULL)";

    @This
    protected DatabaseSQLServiceSpi spi;

    public boolean tableExists( Connection connection )
            throws SQLException
    {
        ResultSet rs = null;
        try {
            String tableNameForQuery = SQLs.TABLE_NAME.toUpperCase();
            rs = connection.getMetaData().getTables( null, null, tableNameForQuery, new String[]{ "TABLE" } );
            boolean tableExists = rs.next();
            LOGGER.trace( "Found table {}? {}", tableNameForQuery, tableExists );
            return tableExists;
        } finally {
            SQLUtil.closeQuietly( rs );
        }
    }

    public String[] buildSQLForTableCreation()
    {
        String[] sql = new String[]{ String.format( CREATE_TABLE_SQL, spi.getCurrentSchemaName() ) };
        LOGGER.trace( "SQL for table creation: {}", sql );
        return sql;
    }

    public EntityValueResult getEntityValue( ResultSet rs )
            throws SQLException
    {
        return new EntityValueResult( rs.getLong( SQLs.ENTITY_PK_COLUMN_NAME ),
                                      rs.getLong( SQLs.ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME ),
                                      new StringReader( rs.getString( SQLs.ENTITY_STATE_COLUMN_NAME ) ) );
    }

}
