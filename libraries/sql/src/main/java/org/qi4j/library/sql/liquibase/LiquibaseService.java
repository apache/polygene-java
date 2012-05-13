/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.sql.liquibase;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.library.sql.common.SQLUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Wrapper service for Liquibase.
 */
@Mixins( LiquibaseService.Mixin.class )
public interface LiquibaseService
        extends Activatable, ServiceComposite
{

    class Mixin
            implements Activatable
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( LiquibaseService.class );

        @This
        Configuration<LiquibaseConfiguration> config;

        @Service
        ServiceReference<DataSource> dataSource;

        public void activate()
                throws Exception
        {
            boolean enabled = config.configuration().enabled().get();
            if ( !enabled || !dataSource.isAvailable() ) {
                return;
            }

            Connection connection = null;
            try {

                connection = dataSource.get().getConnection();
                DatabaseConnection dc = new JdbcConnection( connection );
                Liquibase liquibase = new Liquibase( config.configuration().changeLog().get(), new ClassLoaderResourceAccessor(), dc );
                liquibase.update( config.configuration().contexts().get() );

            } catch ( SQLException e ) {

                Throwable ex = e;
                while ( ex.getCause() != null ) {
                    ex = ex.getCause();
                }

                if ( ex instanceof ConnectException ) {
                    LOGGER.warn( "Could not connect to database; Liquibase should be disabled" );
                    return;
                }

                LOGGER.error( "Liquibase could not perform database migration", e );

            } catch ( ServiceImporterException ex ) {

                LOGGER.warn( "DataSource is not available - database refactoring skipped" );

            } finally {

                SQLUtil.rollbackQuietly( connection );
                SQLUtil.closeQuietly( connection );

            }
        }

        public void passivate()
                throws Exception
        {
        }

    }

}