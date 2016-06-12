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
package org.apache.zest.library.sql.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.zest.api.composite.PropertyMapper;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceImporter;
import org.apache.zest.api.service.ServiceImporterException;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.library.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataSourceServiceImporterMixin<PooledDataSourceType extends DataSource>
        implements ServiceImporter<DataSource>, DataSourceServiceImporterActivation
{

    protected static final Logger LOGGER = LoggerFactory.getLogger( AbstractDataSourceServiceImporterMixin.class );

    private final Map<String, DataSourceConfiguration> configs = new HashMap<>();

    private final Map<String, PooledDataSourceType> pools = new HashMap<>();

    private final Map<DataSource, CircuitBreaker> circuitBreakers = new HashMap<>();

    @Structure
    protected UnitOfWorkFactory uowf;

    @Override
    public final void passivateDataSourceService()
            throws Exception
    {
        for ( PooledDataSourceType pool : pools.values() ) {
            passivateDataSourcePool( pool );
        }
        pools.clear();
        configs.clear();
        circuitBreakers.clear();
    }

    @Override
    public final synchronized DataSource importService( final ImportedServiceDescriptor importedServiceDescriptor )
            throws ServiceImporterException
    {
        PooledDataSourceType pool = pools.get( importedServiceDescriptor.identity() );
        if ( pool == null ) {

            try {

                DataSourceConfiguration config = getConfiguration( importedServiceDescriptor.identity() );
                if ( !config.enabled().get() ) {
                    // Not started
                    throw new ServiceImporterException( "DataSource not enabled" );
                }

                // Instantiate pool
                pool = setupDataSourcePool( config );
                pools.put( importedServiceDescriptor.identity(), pool );

                LOGGER.info( "Starting up DataSource '" + importedServiceDescriptor.identity() + "' for: {}@{}", config.username().get(), config.url().get() );

            } catch ( Exception e ) {
                throw new ServiceImporterException( e );
            }

            // Test the pool
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( null );
            try {
                pool.getConnection().close();
                LOGGER.info( "Database for DataSource is up!" );
            } catch ( SQLException e ) {
                LOGGER.warn( "Database for DataSource " + importedServiceDescriptor.identity() + " is not currently available" );
                throw new ServiceImporterException( "Database for DataSource " + importedServiceDescriptor.identity() + " is not currently available", e );
            } finally {
                Thread.currentThread().setContextClassLoader( cl );
            }
        }

        // Check if circuitbreaker is used
        final CircuitBreaker circuitBreaker = importedServiceDescriptor.metaInfo( CircuitBreaker.class );
        if ( circuitBreaker != null ) {

            DataSource wrappedDataSource = DataSources.wrapWithCircuitBreaker( importedServiceDescriptor.identity(), pool, circuitBreaker );
            circuitBreakers.put( pool, circuitBreaker );
            return wrappedDataSource;

        } else {

            return pool;

        }
    }

    private DataSourceConfiguration getConfiguration( String identity )
            throws InstantiationException
    {
        DataSourceConfiguration config = configs.get( identity );
        if ( config == null ) {
            UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Create DataSource pool configuration" ) );

            try {
                DataSourceConfiguration configEntity = uow.get( DataSourceConfiguration.class, identity );
                config = uow.toValue( DataSourceConfiguration.class, configEntity );
            } catch ( NoSuchEntityException e ) {
                EntityBuilder<DataSourceConfiguration> configBuilder = uow.newEntityBuilder( DataSourceConfiguration.class, identity );

                // Check for defaults
                String s = identity + ".properties";
                InputStream asStream = DataSourceConfiguration.class.getClassLoader().getResourceAsStream( s );
                if ( asStream != null ) {
                    try {
                        PropertyMapper.map( asStream, configBuilder.instance() );
                    } catch ( IOException e1 ) {
                        uow.discard();
                        InstantiationException exception = new InstantiationException( "Could not read underlying Properties file." );
                        exception.initCause( e1 );
                        throw exception;
                    }
                }

                DataSourceConfiguration configEntity = configBuilder.newInstance();
                config = uow.toValue( DataSourceConfiguration.class, configEntity );

                // save
                try {
                    uow.complete();
                } catch ( UnitOfWorkCompletionException e2 ) {
                    InstantiationException exception = new InstantiationException( "Could not save configuration in JavaPreferences." );
                    exception.initCause( e2 );
                    throw exception;
                }

            }

            configs.put( identity, config );
        }

        return config;
    }

    @Override
    public final boolean isAvailable( DataSource instance )
    {
        if ( pools.containsValue( instance ) )
        {
            CircuitBreaker circuitBreaker = circuitBreakers.get( instance );
            return circuitBreaker == null || circuitBreaker.isOn();
        } else {
            return false;
        }
    }

    protected abstract PooledDataSourceType setupDataSourcePool( DataSourceConfiguration configuration )
            throws Exception;

    protected abstract void passivateDataSourcePool( PooledDataSourceType dataSourcePool )
            throws Exception;

}
