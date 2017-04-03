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
package org.apache.polygene.library.sql.liquibase;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;

/**
 * Wrapper service for Liquibase.
 */
@Mixins( LiquibaseService.Mixin.class )
public interface LiquibaseService
{
    /**
     * Creates a new Liquibase instance connected to a visible DataSource.
     *
     * <strong>WARNING</strong> remember to {@literal liquibase.getDatabase().close()}
     *
     * @return a new Liquibase instance connected to a visible DataSource.
     * @throws SQLException if something goes wrong
     * @throws LiquibaseException  if something goes wrong
     */
    Liquibase newConnectedLiquibase() throws SQLException, LiquibaseException;

    /**
     * Apply the configured database changelog.
     *
     * @throws SQLException if something goes wrong
     * @throws LiquibaseException  if something goes wrong
     */
    void applyChangelog() throws SQLException, LiquibaseException;

    /**
     * Apply the configured database changelog.
     *
     * @param parameters changelog parameters, see {@link Liquibase#getChangeLogParameters()}
     * @throws SQLException if something goes wrong
     * @throws LiquibaseException  if something goes wrong
     */
    void applyChangelog( Map<String, Object> parameters )
        throws SQLException, LiquibaseException;

    /**
     * Apply database changelog on application startup.
     *
     * Assembled by {@link LiquibaseAssembler#applyChangelogOnStartup()}.
     *
     * @see LiquibaseService#applyChangelog()
     */
    class ApplyChangelogActivator extends ActivatorAdapter<ServiceReference<LiquibaseService>>
    {
        @Override
        public void afterActivation( ServiceReference<LiquibaseService> activated )
            throws Exception
        {
            activated.get().applyChangelog();
        }
    }

    class Mixin implements LiquibaseService
    {
        @This
        Configuration<LiquibaseConfiguration> config;

        @Service
        ServiceReference<DataSource> dataSource;

        @Override
        public Liquibase newConnectedLiquibase() throws SQLException, LiquibaseException
        {
            config.refresh();
            DatabaseConnection dbConnection = new JdbcConnection( dataSource.get().getConnection() );
            return new Liquibase( config.get().changeLog().get(),
                                  new ClassLoaderResourceAccessor(),
                                  dbConnection );
        }

        @Override
        public void applyChangelog() throws SQLException, LiquibaseException
        {
            applyChangelog( Collections.emptyMap() );
        }

        @Override
        public void applyChangelog( Map<String, Object> parameters )
            throws SQLException, LiquibaseException
        {
            Liquibase liquibase = null;
            try
            {
                liquibase = newConnectedLiquibase();
                for( Map.Entry<String, Object> entry : parameters.entrySet() )
                {
                    liquibase.getChangeLogParameters().set( entry.getKey(), entry.getValue() );
                }
                liquibase.update( config.get().contexts().get() );
            }
            finally
            {
                if( liquibase != null )
                {
                    liquibase.getDatabase().close();
                }
            }
        }
    }
}