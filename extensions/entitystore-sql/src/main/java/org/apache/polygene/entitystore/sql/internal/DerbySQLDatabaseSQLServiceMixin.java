/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.entitystore.sql.internal;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.library.sql.common.SQLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings( "ProtectedField" )
public abstract class DerbySQLDatabaseSQLServiceMixin
    implements DatabaseSQLService, DatabaseSQLStringsBuilder, DatabaseSQLServiceSpi
{
    private static final Logger LOGGER = LoggerFactory.getLogger( DerbySQLDatabaseSQLServiceMixin.class );

    @This
    protected DatabaseSQLServiceSpi spi;

    @Override
    public boolean tableExists( Connection connection )
        throws SQLException
    {
        ResultSet rs = null;
        try
        {
            String tableNameForQuery = SQLs.TABLE_NAME.toUpperCase();
            rs = connection.getMetaData().getTables( null, null, tableNameForQuery,
                                                     new String[] { "TABLE" } );
            boolean tableExists = rs.next();
            LOGGER.trace( "Found table {}? {}", tableNameForQuery, tableExists );
            return tableExists;
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
        }
    }

    @Override
    public Reader getEntityStateReader( ResultSet rs )
        throws SQLException
    {
        return new StringReader( rs.getString( SQLs.ENTITY_STATE_COLUMN_NAME ) );
    }
}
