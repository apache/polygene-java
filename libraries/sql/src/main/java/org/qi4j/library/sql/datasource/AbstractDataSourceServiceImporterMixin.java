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
package org.qi4j.library.sql.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.qi4j.api.composite.PropertyMapper;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.library.circuitbreaker.CircuitBreaker;
import org.qi4j.library.conversion.values.EntityToValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataSourceServiceImporterMixin<PooledDataSourceType extends DataSource>
{

    protected static final Logger LOGGER = LoggerFactory.getLogger( AbstractDataSourceServiceImporterMixin.class );

    protected final Map<String, DataSourceConfigurationValue> configs = new HashMap<String, DataSourceConfigurationValue>();

    protected final Map<String, PooledDataSourceType> pools = new HashMap<String, PooledDataSourceType>();

    protected final Map<PooledDataSourceType, CircuitBreaker> circuitBreakers = new HashMap<PooledDataSourceType, CircuitBreaker>();

    @Structure
    protected Module module;

    @Service
    private EntityToValue entityToValue;

    public final void activate()
            throws Exception
    {
        onActivate();
    }

    protected void onActivate()
            throws Exception
    {
    }

    public final void passivate()
            throws Exception
    {
        for ( PooledDataSourceType pool : pools.values() ) {
            passivateDataSourcePool( pool );
        }

        pools.clear();
        configs.clear();
        circuitBreakers.clear();

        onPassivate();
    }

    protected void onPassivate()
            throws Exception
    {
    }

    public final synchronized Object importService( final ImportedServiceDescriptor importedServiceDescriptor )
            throws ServiceImporterException
    {
        PooledDataSourceType pool = pools.get( importedServiceDescriptor.identity() );
        if ( pool == null ) {

            try {

                DataSourceConfigurationValue config = getConfiguration( importedServiceDescriptor.identity() );
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

    private DataSourceConfigurationValue getConfiguration( String identity )
            throws InstantiationException
    {
        DataSourceConfigurationValue config = configs.get( identity );
        if ( config == null ) {
            UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Create DataSource pool configuration" ) );

            try {
                DataSourceConfiguration configEntity = uow.get( DataSourceConfiguration.class, identity );
                config = entityToValue.convert( DataSourceConfigurationValue.class, configEntity );
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
                config = entityToValue.convert( DataSourceConfigurationValue.class, configEntity );

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

    public final boolean isActive( Object instance )
    {
        return pools.containsValue( instance );
    }

    public final boolean isAvailable( Object instance )
    {
        if ( pools.containsValue( instance ) ) {
            CircuitBreaker circuitBreaker = circuitBreakers.get( instance );
            if ( circuitBreaker != null ) {
                return circuitBreaker.isOn();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    protected abstract PooledDataSourceType setupDataSourcePool( DataSourceConfigurationValue configuration )
            throws Exception;

    protected abstract void passivateDataSourcePool( PooledDataSourceType dataSourcePool )
            throws Exception;

}
