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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.qi4j.api.composite.PropertyMapper;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.library.circuitbreaker.CircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataSourceServiceImporterMixin<PooledDataSourceType extends DataSource>
{

    protected static final Logger LOGGER = LoggerFactory.getLogger( AbstractDataSourceServiceImporterMixin.class );

    protected final Map<String, DataSourceConfiguration> configs = new HashMap<String, DataSourceConfiguration>();

    protected final Map<String, PooledDataSourceType> pools = new HashMap<String, PooledDataSourceType>();

    protected final Map<PooledDataSourceType, CircuitBreaker> circuitBreakers = new HashMap<PooledDataSourceType, CircuitBreaker>();

    @Structure
    protected Module module;

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

        // WARN Closes all configuration UoWs
        for ( DataSourceConfiguration dataSourceConfiguration : configs.values() ) {
            module.getUnitOfWork( dataSourceConfiguration ).discard();
        }
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

            circuitBreakers.put( pool, circuitBreaker );
            return wrapWithCircuitBreaker( importedServiceDescriptor, pool, circuitBreaker );

        } else {

            return pool;

        }
    }

    private Object wrapWithCircuitBreaker( final ImportedServiceDescriptor importedServiceDescriptor, final PooledDataSourceType pool, final CircuitBreaker circuitBreaker )
    {
        // Create wrapper
        InvocationHandler handler = new InvocationHandler()
        {

            @Override
            public Object invoke( Object proxy, Method method, Object[] args )
                    throws Throwable
            {
                if ( !circuitBreaker.isOn() ) {
                    Throwable throwable = circuitBreaker.getLastThrowable();
                    if ( throwable != null ) {
                        throw throwable;
                    } else {
                        throw new ServiceImporterException( "Circuit breaker for DataSource " + importedServiceDescriptor.identity() + " is not on" );
                    }
                }

                try {
                    Object result = method.invoke( pool, args );
                    circuitBreaker.success();
                    return result;
                } catch ( IllegalAccessException e ) {
                    circuitBreaker.throwable( e );
                    throw e;
                } catch ( IllegalArgumentException e ) {
                    circuitBreaker.throwable( e );
                    throw e;
                } catch ( InvocationTargetException e ) {
                    circuitBreaker.throwable( e.getCause() );
                    throw e.getCause();
                }
            }

        };

        // Create proxy with circuit breaker
        return Proxy.newProxyInstance( DataSource.class.getClassLoader(), new Class[]{ DataSource.class }, handler );
    }

    private DataSourceConfiguration getConfiguration( String identity )
            throws InstantiationException
    {
        DataSourceConfiguration config = configs.get( identity );
        if ( config == null ) {
            UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Create DataSource pool configuration" ) );

            try {
                config = uow.get( DataSourceConfiguration.class, identity );
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

                config = configBuilder.newInstance();

                // save
                try {
                    uow.complete();
                } catch ( UnitOfWorkCompletionException e2 ) {
                    InstantiationException exception = new InstantiationException( "Could not save configuration in JavaPreferences." );
                    exception.initCause( e2 );
                    throw exception;
                }

                // Create new uow and fetch entity
                // WARN This UoW is closed on Service passivation
                uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Read DataSource pool configuration" ) );
                config = uow.get( DataSourceConfiguration.class, identity );
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

    protected abstract PooledDataSourceType setupDataSourcePool( DataSourceConfiguration configuration )
            throws Exception;

    protected abstract void passivateDataSourcePool( PooledDataSourceType dataSourcePool )
            throws Exception;

}
