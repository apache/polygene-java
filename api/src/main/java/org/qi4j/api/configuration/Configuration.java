/*
 * Copyright (c) 2008, Rickard �berg. All Rights Reserved.
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

package org.qi4j.api.configuration;

import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.PropertyMapper;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provide Configurations for Services. A Service that wants to be configurable
 * should inject a reference to Configuration with the Configuration type:
 * <code><pre>
 * <p/>
 * &#64;This Configuration&#60;MyServiceConfiguration&#62; config;
 * </pre></code>
 * where MyServiceConfiguration extends {@link ConfigurationComposite}, which itself is an ordinary
 * {@link org.qi4j.api.entity.EntityComposite}. The Configuration implementation
 * will either locate an instance of the given Configuration type in the
 * persistent store using the identity of the Service, or create a new such instance
 * if one doesn't already exist.
 * <p>
 * If a new Configuration instance is created then it will be populated with properties
 * from the properties file whose filesystem name is the same as the identity (e.g. "MyService.properties").
 * If a service is not given a name via the {@link org.qi4j.bootstrap.ServiceDeclaration#identifiedBy(String)}, the
 * name will default to the FQCN of the ServiceComposite type.
 * </p>
 * <p>
 * The Configuration instance can be modified externally just like any other EntityComposite, but
 * its values will not be updated in the Service until {@link #refresh()} is called. This allows
 * safe reloads of Configuration state to ensure that it is not reloaded while the Service is handling
 * a request.
 * </p>
 * <p>
 * The Configuration will be automatically refreshed when the Service is activated through the
 * {@link org.qi4j.api.service.Activatable#activate()} method by the Qi4j runtime.
 * Any refreshes at other points will have to be done manually or triggered through some other
 * mechanism.
 * </p>
 * <p>
 * The user configuration entity is part of a long running {@link UnitOfWork}, and to persist changes to it the
 * {@link #save()} method must be called. No other actions are required. Example;
 * <pre><code>
 * <p/>
 * public interface MyConfiguration extends ConfigurationComposite
 * {
 *     Property&lt;Long&gt; timeout();
 * }
 * <p/>
 * :
 * <p/>
 * <p/>
 * &#64;This Configuration&lt;MyConfiguration&gt; config;
 * <p/>
 * <p/>
 * :
 * private void setTimeoutConfiguration( long timeout )
 * {
 *     config.configuration().timeout().set( timeout );
 *     config.save();
 * }
 * </code></pre>
 * And even if a separate thread is using the {@code timeout()} configuration when this is happening, the
 * {@link UnitOfWork} isolation will ensure that the other thread is not affected. That thread, on the other hand
 * will need to do a {@link #refresh()} at an appropriate time to pick up the timeout change. For instance;
 * <code><pre>
 * <p/>
 * </pre></code>
 * </p>
 */
@Mixins( Configuration.ConfigurationMixin.class )
public interface Configuration<T>
{
    /**
     * Retrieves the user configuration instance managed by this Configuration.
     * <p/>
     * <p>
     * Even if the user configuration is initialized from properties file, the consistency rules of Qi4j composites
     * still applies. If the the properties file is missing a value, then the initialization will fail with a
     * RuntimeException. If Constraints has been defined, those will need to be satisfied as well. The user
     * configuration instance returned will fulfill the constraints and consistency normal to all composites, and
     * can therefor safely be used with additional checks.
     * </p>
     *
     * @return The fully initialized and ready-to-use user configuration instance.
     */
    T configuration();

    /**
     * Updates the values of the managed user ConfigurationComposite instance from the underlying
     * {@link org.qi4j.spi.entitystore.EntityStore}.  Any modified values in the current user configuration that
     * has not been saved, via {@link #save()} method, will be lost.
     */
    void refresh();

    /**
     * Persists the modified values in the user configuration instance to the underlying store.
     * <p>
     *
     * </p>
     */
    void save();

    /**
     * Implementation of Configuration.
     * <p>
     * This is effectively an internal class in Qi4j and should never be used directly by user code.
     * </p>
     *
     * @param <T>
     */
    public class ConfigurationMixin<T>
        implements Configuration<T>, Activatable
    {
        private T configuration;
        private UnitOfWork uow;

        @Structure
        private Qi4j api;

        @This
        private ServiceComposite me;

        @Structure
        private UnitOfWorkFactory uowf;

        public ConfigurationMixin()
        {
        }

        public synchronized T configuration()
        {
            if( configuration == null )
            {
                uow = uowf.newUnitOfWork();
                try
                {
                    configuration = this.<T>getConfigurationInstance( me, uow );
                }
                catch( InstantiationException e )
                {
                    throw new IllegalStateException( e );
                }
            }

            return configuration;
        }

        public synchronized void refresh()
        {
            if( configuration != null )
            {
                configuration = null;
                uow.discard();
                uow = null;
            }
        }

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

        public void activate()
            throws Exception
        {
            refresh();
        }

        public void passivate()
            throws Exception
        {
        }

    public <T> T getConfigurationInstance( ServiceComposite serviceComposite, UnitOfWork uow )
        throws InstantiationException
    {
        ServiceDescriptor serviceModel = api.getServiceDescriptor( serviceComposite );

        String identity = serviceComposite.identity().get();
        T configuration;
        try
        {
            configuration = uow.get( serviceModel.<T>configurationType(), identity );
            uow.pause();
        }
        catch( NoSuchEntityException e )
        {
            return (T) initializeConfigurationInstance( serviceComposite, uow, serviceModel, identity );
        }
        catch( EntityTypeNotFoundException e )
        {
            return (T) initializeConfigurationInstance( serviceComposite, uow, serviceModel, identity );
        }
        return (T) configuration;
    }

    private <T> T initializeConfigurationInstance( ServiceComposite serviceComposite,
                                                   UnitOfWork uow,
                                                   ServiceDescriptor serviceModel,
                                                   String identity
    )
        throws InstantiationException
    {
        T configuration;
        Module module = api.getModule( serviceComposite );
        UnitOfWork buildUow = module.newUnitOfWork();

        EntityBuilder<T> configBuilder = buildUow.newEntityBuilder( serviceModel.<T>configurationType(), identity );

        // Check for defaults
        String s = identity + ".properties";
        InputStream asStream = api.getServiceDescriptor( serviceComposite).type().getResourceAsStream( s );
        if( asStream != null )
        {
            try
            {
                PropertyMapper.map( asStream, (Composite) configBuilder.instance() );
            }
            catch( IOException e1 )
            {
                InstantiationException exception = new InstantiationException(
                    "Could not read underlying Properties file." );
                exception.initCause( e1 );
                throw exception;
            }
        }

        try
        {
            configuration = configBuilder.newInstance();
            buildUow.complete();

            // Try again
            return (T) getConfigurationInstance( serviceComposite, uow );
        }
        catch( Exception e1 )
        {
            InstantiationException ex = new InstantiationException(
                "Could not instantiate configuration, and no Properties file was found (" + s + ")" );
            ex.initCause( e1 );
            throw ex;
        }
    }
    }
}
