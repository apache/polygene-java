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
package org.apache.zest.entitystore.sql.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Application.Mode;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.library.sql.common.SQLConfiguration;
import org.apache.zest.library.sql.common.SQLUtil;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.vendor.SQLVendor;

@SuppressWarnings( "ProtectedField" )
public abstract class DatabaseSQLServiceCoreMixin
        implements DatabaseSQLService
{

    private static final Logger LOGGER = LoggerFactory.getLogger( DatabaseSQLServiceCoreMixin.class );

    @Structure
    private Application application;

    @Service
    private DataSource dataSource;

    @This
    private DatabaseSQLServiceState state;

    @This
    protected DatabaseSQLServiceSpi spi;

    @This
    private DatabaseSQLStringsBuilder sqlStrings;

    @Uses
    private ServiceDescriptor descriptor;

    @This
    private Configuration<SQLConfiguration> configuration;

    @Override
    public Connection getConnection()
            throws SQLException
    {
        return dataSource.getConnection();
    }

    @Override
    public void startDatabase()
            throws Exception
    {
        Connection connection = getConnection();
        String schema = this.getConfiguredSchemaName( SQLs.DEFAULT_SCHEMA_NAME );
        if ( schema == null ) {
            throw new EntityStoreException( "Schema name must not be null." );
        } else {
            state.schemaName().set( schema );
            state.vendor().set( this.descriptor.metaInfo( SQLVendor.class ) );

            sqlStrings.init();

            if ( !spi.schemaExists( connection ) ) {
                LOGGER.debug( "Database Schema '{}' NOT found!", schema );
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    for ( String sql : sqlStrings.buildSQLForSchemaCreation() ) {
                        stmt.execute( sql );
                    }
                } finally {
                    SQLUtil.closeQuietly( stmt );
                }
                LOGGER.debug( "Database Schema '{}' created", schema );
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

        }

        SQLUtil.closeQuietly( connection );

    }

    @Override
    public void stopDatabase()
            throws Exception
    {
        if ( Mode.production == application.mode() ) {
            // NOOP
        }
    }

    /**
     * Configuration is optional at both assembly and runtime.
     */
    protected String getConfiguredSchemaName( String defaultSchemaName )
    {
        if ( configuration == null ) {
            NullArgumentException.validateNotNull( "default schema name", defaultSchemaName );
            LOGGER.debug( "No configuration, will use default schema name: '{}'", defaultSchemaName );
            return defaultSchemaName;
        }
        String result = configuration.get().schemaName().get();
        if ( result == null ) {
            NullArgumentException.validateNotNull( "default schema name", defaultSchemaName );
            result = defaultSchemaName;
            LOGGER.debug( "No database schema name in configuration, will use default: '{}'", defaultSchemaName );
        } else {
            LOGGER.debug( "Will use configured database schema name: '{}'", result );
        }
        return result;
    }

}
