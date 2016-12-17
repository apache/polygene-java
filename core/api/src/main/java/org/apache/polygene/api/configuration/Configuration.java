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

package org.apache.polygene.api.configuration;

import java.io.IOException;
import java.io.InputStream;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.PropertyMapper;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.service.qualifier.ServiceTags;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.NoSuchEntityTypeException;
import org.apache.polygene.api.unitofwork.NoSuchEntityException;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.api.usecase.UsecaseBuilder;
import org.apache.polygene.api.value.ValueSerialization;

/**
 * Provide Configurations for Services. A Service that wants to be configurable
 * should inject a reference to Configuration with the Configuration type:
 * <pre><code>
 *  * &#64;This Configuration&#60;MyServiceConfiguration&#62; config;
 * </code></pre>
 * <p>
 * where MyServiceConfiguration extends {@link ConfigurationComposite}, which itself is an ordinary
 * {@link org.apache.polygene.api.entity.EntityComposite}. The Configuration implementation
 * will either locate an instance of the given Configuration type in the
 * persistent store using the reference of the Service, or create a new such instance
 * if one doesn't already exist.
 * </p>
 * <p>
 * If a new Configuration instance is created then it will be populated with properties
 * from the properties file whose filesystem name is the same as the reference (e.g. "MyService.properties").
 * If a service is not given a name via the {@code org.apache.polygene.bootstrap.ServiceDeclaration#identifiedBy(String)}, the
 * name will default to the FQCN of the ServiceComposite type.
 * </p>
 * <p>
 * The Configuration instance can be modified externally just like any other EntityComposite, but
 * its values will not be updated in the Service until {@link #refresh()} is called. This allows
 * safe reloads of Configuration state to ensure that it is not reloaded while the Service is handling
 * a request.
 * </p>
 * <p>
 * The Configuration will be automatically refreshed when the Service is activated by the Polygene runtime.
 * Any refreshes at other points will have to be done manually or triggered through some other
 * mechanism.
 * </p>
 * <p>
 * The user configuration entity is part of a long running {@link UnitOfWork}, and to persist changes to it the
 * {@link #save()} method must be called. No other actions are required. Example;
 * </p>
 * <pre><code>
 *
 * public interface MyConfiguration extends ConfigurationComposite
 * {
 *     Property&lt;Long&gt; timeout();
 * }
 *
 * :
 *
 * &#64;This Configuration&lt;MyConfiguration&gt; config;
 * :
 * private void setTimeoutConfiguration( long timeout )
 * {
 *     config.get().timeout().set( timeout );
 *     config.save();
 * }
 * </code></pre>
 * <p>
 * And even if a separate thread is using the {@code timeout()} configuration when this is happening, the
 * {@link UnitOfWork} isolation will ensure that the other thread is not affected. That thread, on the other hand
 * will need to do a {@link #refresh()} at an appropriate time to pick up the timeout change. For instance;
 * </p>
 * <pre><code>
 *
 * &#64;Service InventoryService remoteInventoryService;
 *
 * public void restockInventoryItem( InventoryItemId id, int itemCount )
 * {
 *     config.refresh();
 *     long timeout = config.get().timeout().get();
 *
 *     remoteInventoryService.restock( id, itemCount, timeout );
 *
 *     :
 *     :
 * }
 * </code></pre>
 *
 * @param <T> Configuration type
 */
@SuppressWarnings( "JavadocReference" )
@Mixins( Configuration.ConfigurationMixin.class )
public interface Configuration<T>
{
    /**
     * Retrieves the user configuration instance managed by this Configuration.
     * <p>
     * Even if the user configuration is initialized from properties file, the consistency rules of Polygene composites
     * still applies. If the the properties file is missing a value, then the initialization will fail with a
     * RuntimeException. If Constraints has been defined, those will need to be satisfied as well. The user
     * configuration instance returned will fulfill the constraints and consistency normal to all composites, and
     * can therefor safely be used with additional checks.
     * </p>
     *
     * @return The fully initialized and ready-to-use user configuration instance.
     */
    T get();

    /**
     * Updates the values of the managed user ConfigurationComposite instance from the underlying
     * {@code org.apache.polygene.spi.entitystore.EntityStore}.  Any modified values in the current user configuration that
     * has not been saved, via {@link #save()} method, will be lost.
     */
    void refresh();

    /**
     * Persists the modified values in the user configuration instance to the underlying store.
     */
    void save();

    /**
     * Implementation of Configuration.
     * <p>
     * This is effectively an internal class in Polygene and should never be used directly by user code.
     * </p>
     *
     * @param <T> Configuration type
     */
    class ConfigurationMixin<T>
        implements Configuration<T>
    {
        private T configuration;
        private UnitOfWork uow;

        @Structure
        private PolygeneAPI api;

        @This
        private ServiceComposite me;

        @Structure
        private UnitOfWorkFactory uowf;

        @Service
        private Iterable<ServiceReference<ValueSerialization>> valueSerialization;


        public ConfigurationMixin()
        {
        }

        @Override
        public synchronized T get()
        {
            if( configuration == null )
            {
                Usecase usecase = UsecaseBuilder.newUsecase( "Configuration:" + me.identity().get() );
                uow = uowf.newUnitOfWork( usecase );
                try
                {
                    configuration = this.findConfigurationInstanceFor( me, uow );
                }
                catch( InstantiationException e )
                {
                    throw new IllegalStateException( e );
                }
            }

            return configuration;
        }

        @Override
        public synchronized void refresh()
        {
            if( configuration != null )
            {
                configuration = null;
                uow.discard();
                uow = null;
            }
        }

        @Override
        public void save()
        {
            if( uow != null )
            {
                try
                {
                    uow.complete();
                    uow = null;
                }
                catch( UnitOfWorkCompletionException e )
                {
                    // Should be impossible
                    e.printStackTrace();
                }

                configuration = null; // Force refresh
            }
        }

        @SuppressWarnings( "unchecked" )
        public <V> V findConfigurationInstanceFor( ServiceComposite serviceComposite, UnitOfWork uow )
            throws InstantiationException
        {
            ServiceDescriptor serviceModel = api.serviceDescriptorFor( serviceComposite );

            V configuration;
            try
            {
                configuration = uow.get( serviceModel.<V>configurationType(), serviceComposite.identity().get() );
                uow.pause();
            }
            catch( NoSuchEntityException | NoSuchEntityTypeException e )
            {
                return (V) initializeConfigurationInstance( serviceComposite, uow, serviceModel, serviceComposite.identity().get() );
            }
            return configuration;
        }

        @SuppressWarnings( "unchecked" )
        private <V extends HasIdentity> V initializeConfigurationInstance(ServiceComposite serviceComposite,
                                                                          UnitOfWork uow,
                                                                          ServiceDescriptor serviceModel,
                                                                          Identity identity
        )
            throws InstantiationException
        {
            Module module = api.moduleOf( serviceComposite ).instance();
            Usecase usecase = UsecaseBuilder.newUsecase( "Configuration:" + me.identity().get() );
            UnitOfWork buildUow = module.unitOfWorkFactory().newUnitOfWork( usecase );

            Class<?> type = api.serviceDescriptorFor( serviceComposite ).types().findFirst().orElse( null );
            Class<V> configType = serviceModel.configurationType();

            // Check for defaults
            V config = tryLoadPropertiesFile( buildUow, type, configType, identity );
            if( config == null )
            {
                config = tryLoadJsonFile( buildUow, type, configType, identity );
                if( config == null )
                {
                    config = tryLoadYamlFile( buildUow, type, configType, identity );
                    if( config == null )
                    {
                        config = tryLoadXmlFile( buildUow, type, configType, identity );
                        if( config == null )
                        {
                            try
                            {
                                EntityBuilder<V> configBuilder = buildUow.newEntityBuilder( serviceModel.<V>configurationType(), identity );
                                configBuilder.newInstance();
                            }
                            catch( ConstraintViolationException e )
                            {
                                throw new NoSuchConfigurationException( configType, identity, e );
                            }
                        }
                    }
                }
            }

            try
            {
                buildUow.complete();

                // Try again
                return (V) findConfigurationInstanceFor( serviceComposite, uow );
            }
            catch( Exception e1 )
            {
                InstantiationException ex = new InstantiationException(
                    "Could not instantiate configuration, and no configuration initialization file was found (" + identity + ")" );
                ex.initCause( e1 );
                throw ex;
            }
        }

        private <C, V> V tryLoadPropertiesFile( UnitOfWork buildUow,
                                                Class<C> compositeType,
                                                Class<V> configType,
                                                Identity identity
        )
            throws InstantiationException
        {
            EntityBuilder<V> configBuilder = buildUow.newEntityBuilder( configType, identity );
            String resourceName = identity + ".properties";
            InputStream asStream = getResource( compositeType, resourceName );
            if( asStream != null )
            {
                try
                {
                    PropertyMapper.map( asStream, (Composite) configBuilder.instance() );
                    return configBuilder.newInstance();
                }
                catch( IOException e1 )
                {
                    InstantiationException exception = new InstantiationException(
                        "Could not read underlying Properties file." );
                    exception.initCause( e1 );
                    throw exception;
                }
            }
            return null;
        }

        private InputStream getResource( Class<?> type, String resourceName )
        {
            // Load defaults from classpath root if available
            if( type.getResource( resourceName ) == null && type.getResource( "/" + resourceName ) != null )
            {
                resourceName = "/" + resourceName;
            }
            return type.getResourceAsStream( resourceName );
        }

        private <C, V extends HasIdentity> V tryLoadJsonFile(UnitOfWork uow,
                                                             Class<C> compositeType,
                                                             Class<V> configType,
                                                             Identity identity
        )
        {
            return readConfig( uow, compositeType, configType, identity, ValueSerialization.Formats.JSON, ".json" );
        }

        private <C, V extends HasIdentity> V tryLoadYamlFile(UnitOfWork uow,
                                                             Class<C> compositeType,
                                                             Class<V> configType,
                                                             Identity identity
        )
        {
            return readConfig( uow, compositeType, configType, identity, ValueSerialization.Formats.YAML, ".yaml" );
        }

        private <C, V extends HasIdentity> V tryLoadXmlFile(UnitOfWork uow,
                                                            Class<C> compositeType,
                                                            Class<V> configType,
                                                            Identity identity
        )
        {
            return readConfig( uow, compositeType, configType, identity, ValueSerialization.Formats.XML, ".xml" );
        }

        private <C, V extends HasIdentity> V readConfig(UnitOfWork uow,
                                                        Class<C> compositeType,
                                                        Class<V> configType,
                                                        Identity identity,
                                                        String format,
                                                        String extension
        )
        {
            for( ServiceReference<ValueSerialization> serializerRef : valueSerialization )
            {
                ServiceTags serviceTags = serializerRef.metaInfo( ServiceTags.class );
                if( serviceTags.hasTag( format ) )
                {
                    String resourceName = identity + extension;
                    InputStream asStream = getResource( compositeType, resourceName );
                    if( asStream != null )
                    {
                        V configObject = serializerRef.get().deserialize( uow.module(), configType, asStream );
                        return uow.toEntity( configType, configObject );
                    }
                }
            }
            return null;
        }
    }
}
