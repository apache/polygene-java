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
import java.util.Properties;

import javax.sql.DataSource;

import org.qi4j.api.composite.PropertyMapper;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.library.circuitbreaker.CircuitBreaker;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataSource service implemented as a ServiceImporter.
 * 
 * Import visible DataSources as Services. The default Mixin use the C3P0
 * pooling system optionaly wrapped with CircuitBreaker using a proxy.
 * 
 * TODO Rename to C3P0DataSourceServiceImporter
 */
@Mixins(C3P0DataSourceServiceImporter.Mixin.class)
public interface C3P0DataSourceServiceImporter
        extends ServiceImporter, Activatable, ServiceComposite
{
    class Mixin
            implements Activatable, ServiceImporter
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( C3P0DataSourceServiceImporter.class );
        
        @Structure
        Module module;

        Map<String, ComboPooledDataSource> pools = new HashMap<String, ComboPooledDataSource>();
        Map<String, DataSourceConfiguration> configs = new HashMap<String, DataSourceConfiguration>();
        Map<ComboPooledDataSource, CircuitBreaker> circuitBreakers = new HashMap<ComboPooledDataSource, CircuitBreaker>();
        
        public void activate() throws Exception
        {
        }

        public void passivate() throws Exception
        {
            for( ComboPooledDataSource pool : pools.values() )
            {
                DataSources.destroy( pool );
            }
            pools.clear();

            // WARN Closes all configuration UoWs
            for( DataSourceConfiguration dataSourceConfiguration : configs.values() )
            {
                module.getUnitOfWork( dataSourceConfiguration ).discard();
            }
            configs.clear();

            circuitBreakers.clear();
        }

        public synchronized Object importService( final ImportedServiceDescriptor importedServiceDescriptor ) throws ServiceImporterException
        {
            ComboPooledDataSource pool = pools.get( importedServiceDescriptor.identity() );
            if( pool == null )
            {
                // Instantiate pool
                pool = new ComboPooledDataSource();

                try
                {
                    DataSourceConfiguration config = getConfiguration( importedServiceDescriptor.identity() );

                    if( config.enabled().get() )
                    {
                        Class.forName( config.driver().get() );
                        pool.setDriverClass( config.driver().get() );
                        pool.setJdbcUrl( config.url().get() );

                        String props = config.properties().get();
                        String[] properties = props.split( "," );
                        Properties poolProperties = new Properties();
                        for( String property : properties )
                        {
                            if( property.trim().length() > 0 )
                            {
                                String[] keyvalue = property.trim().split( "=" );
                                poolProperties.setProperty( keyvalue[0], keyvalue[1] );
                            }
                        }
                        pool.setProperties( poolProperties );

                        if( !config.username().get().equals( "" ) )
                        {
                            pool.setUser( config.username().get() );
                            pool.setPassword( config.password().get() );
                        }
                        pool.setMaxConnectionAge( 60 * 60 ); // One hour max age

                        LOGGER.info( "Starting up DataSource '" + importedServiceDescriptor.identity() + "' for:{}", pool.getUser() + "@" + pool.getJdbcUrl() );
                    } else
                    {
                        // Not started
                        throw new ServiceImporterException( "DataSource not enabled" );
                    }

                    pools.put( importedServiceDescriptor.identity(), pool );
                } catch( Exception e )
                {
                    throw new ServiceImporterException( e );
                }

                // Test the pool
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader( null );
                try
                {
                    pool.getConnection().close();
                    LOGGER.info( "Database for DataSource is up!" );
                } catch( SQLException e )
                {
                    LOGGER.warn( "Database for DataSource " + importedServiceDescriptor.identity() + " is not currently available" );
                    throw new ServiceImporterException( "Database for DataSource " + importedServiceDescriptor.identity() + " is not currently available", e );
                } finally
                {
                    Thread.currentThread().setContextClassLoader( cl );
                }
            }


            // Check if circuitbreaker is used
            final CircuitBreaker circuitBreaker = importedServiceDescriptor.metaInfo( CircuitBreaker.class );
            if (circuitBreaker != null)
            {
                circuitBreakers.put( pool, circuitBreaker );

                final DataSource invokedPool = pool;

                // Create wrapper
                InvocationHandler handler = new InvocationHandler()
                {
                    @Override
                    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
                    {
                        if (!circuitBreaker.isOn())
                        {
                            Throwable throwable = circuitBreaker.getLastThrowable();
                            if (throwable != null)
                                throw throwable;
                            else
                                throw new ServiceImporterException("Circuit breaker for DataSource "+importedServiceDescriptor.identity()+" is not on");
                        }

                        try
                        {
                            Object result = method.invoke( invokedPool, args );
                            circuitBreaker.success();
                            return result;
                        } catch( IllegalAccessException e )
                        {
                            circuitBreaker.throwable( e );
                            throw e;
                        } catch( IllegalArgumentException e )
                        {
                            circuitBreaker.throwable( e );
                            throw e;
                        } catch( InvocationTargetException e )
                        {
                            circuitBreaker.throwable( e.getCause() );
                            throw e.getCause();
                        }
                    }
                };

                // Create proxy with circuit breaker
                return Proxy.newProxyInstance( DataSource.class.getClassLoader(), new Class[]{DataSource.class}, handler );
            } else
                return pool;
        }

        private DataSourceConfiguration getConfiguration( String identity ) throws InstantiationException
        {
            DataSourceConfiguration config = configs.get( identity );
            if( config == null )
            {
                UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Create DataSource pool configuration" ) );

                try
                {
                    config = uow.get( DataSourceConfiguration.class, identity );
                } catch( NoSuchEntityException e )
                {
                    EntityBuilder<DataSourceConfiguration> configBuilder = uow.newEntityBuilder( DataSourceConfiguration.class, identity );

                    // Check for defaults
                    String s = identity + ".properties";
                    InputStream asStream = DataSourceConfiguration.class.getClassLoader().getResourceAsStream( s );
                    if( asStream != null )
                    {
                        try
                        {
                            PropertyMapper.map( asStream, configBuilder.instance() );
                        } catch( IOException e1 )
                        {
                            uow.discard();
                            InstantiationException exception = new InstantiationException( "Could not read underlying Properties file." );
                            exception.initCause( e1 );
                            throw exception;
                        }
                    }

                    config = configBuilder.newInstance();

                    // save
                    try
                    {
                        uow.complete();
                    } catch( UnitOfWorkCompletionException e2 )
                    {
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

        public boolean isActive( Object instance )
        {
            return pools.containsValue( instance );
        }

        public boolean isAvailable( Object instance )
        {
            if (pools.containsValue( instance ))
            {
                CircuitBreaker circuitBreaker = circuitBreakers.get( instance );
                if (circuitBreaker != null)
                    return circuitBreaker.isOn();
                else
                    return true;
            } else
                return false;
        }
    }
}