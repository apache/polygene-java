/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.runtime.service;

import java.lang.reflect.Method;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationEvent;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.ServiceUnavailableException;
import org.qi4j.api.structure.Module;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.qi4j.runtime.activation.ActivationEventListenerSupport;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * Implementation of ServiceReference. This manages the actual instance of the service
 * and implements the service Activation.
 * <p/>
 * Whenever the service is requested a proxy is returned which points to this class. This means
 * that the instance can be passivated even though a client is holding on to a service proxy.
 */
public final class ServiceReferenceInstance<T>
    implements ServiceReference<T>, Activation
{
    private volatile ServiceInstance instance;
    private final T serviceProxy;
    private final ModuleInstance module;
    private final ServiceModel serviceModel;
    private final ActivationDelegate activation = new ActivationDelegate( this );
    private final ActivationEventListenerSupport activationEventSupport = new ActivationEventListenerSupport();
    private boolean active = false;

    public ServiceReferenceInstance( ServiceModel serviceModel, ModuleInstance module )
    {
        this.module = module;
        this.serviceModel = serviceModel;

        serviceProxy = newProxy();
    }

    @Override
    public String identity()
    {
        return serviceModel.identity();
    }

    @Override
    public Iterable<Class<?>> types()
    {
        return serviceModel.types();
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return serviceModel.metaInfo( infoType );
    }

    @Override
    public synchronized T get()
    {
        return serviceProxy;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    public boolean isAvailable()
    {
        return getInstance().isAvailable();
    }

    public Module module()
    {
        return module;
    }

    @Override
    public void activate()
        throws ActivationException
    {
        if( serviceModel.isInstantiateOnStartup() )
        {
            getInstance();
        }
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        if( instance != null )
        {
            try {
                activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
                activation.passivate( new Runnable()
                {

                    @Override
                    public void run()
                    {
                        active = false;
                    }

                } );
                activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
            } finally {
                instance = null;
                active = false;
            }
        }
    }

    private ServiceInstance getInstance()
        throws ServiceImporterException
    {
        // DCL that works with Java 1.5 volatile semantics
        if( instance == null )
        {
            synchronized( this )
            {
                if( instance == null )
                {
                    instance = serviceModel.newInstance( module );

                    try
                    {
                        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATING ) );
                        activation.activate( serviceModel.newActivatorsInstance(), instance, new Runnable()
                        {

                            @Override
                            public void run()
                            {
                                active = true;
                            }

                        } );
                        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
                    }
                    catch( Exception e )
                    {
                        instance = null;
                        throw new ServiceUnavailableException( "Could not activate service " + serviceModel.identity(), e );
                    }
                }
            }
        }

        return instance;
    }

    @Override
    public String toString()
    {
        return serviceModel.identity() + "(active=" + isActive() + ",module='" + module.name() + "')";
    }

    public T newProxy()
    {
        return (T) serviceModel.newProxy( new ServiceReferenceInstance.ServiceInvocationHandler() );
    }

    public ServiceDescriptor serviceDescriptor()
    {
        return serviceModel;
    }

    public final class ServiceInvocationHandler
        implements CompositeInstance
    {
        @Override
        public <T> T proxy()
        {
            return (T) ServiceReferenceInstance.this.get();
        }

        @Override
        public <T> T newProxy( Class<T> mixinType )
            throws IllegalArgumentException
        {
            return getInstance().newProxy( mixinType );
        }

        @Override
        public <T> T metaInfo( Class<T> infoType )
        {
            return ServiceReferenceInstance.this.metaInfo( infoType );
        }

        @Override
        public Iterable<Class<?>> types()
        {
            return ServiceReferenceInstance.this.types();
        }

        @Override
        public CompositeDescriptor descriptor()
        {
            return ServiceReferenceInstance.this.serviceDescriptor();
        }

        @Override
        public Object invokeComposite( Method method, Object[] args )
            throws Throwable
        {
            return getInstance().invokeComposite( method, args );
        }

        @Override
        public StateHolder state()
        {
            return getInstance().state();
        }

        @Override
        public Object invoke( Object object, Method method, Object[] objects )
            throws Throwable
        {
            if( method.getDeclaringClass().equals( Object.class ) )
            {
                if( method.getName().equals( "toString" ) )
                {
                    return serviceModel.toString();
                }
                else if( method.getName().equals( "equals" ) )
                {
                    Object obj = objects[ 0 ];
                    return obj == object;
                }
                else if( method.getName().equals( "hashCode" ) )
                {
                    return serviceModel.toString().hashCode();
                }
            }

            ServiceInstance instance = getInstance();

/*
            if (!instance.isAvailable())
            {
                throw new ServiceUnavailableException("Service is currently not available");
            }

*/
            return instance.invoke( object, method, objects );
        }

        @Override
        public String toString()
        {
            return serviceModel.toString();
        }

        @Override
        public Module module()
        {
            return module;
        }
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.deregisterActivationEventListener( listener );
    }

    @Override
    public int hashCode()
    {
        return identity().hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final ServiceReference other = ( ServiceReference ) obj;
        return identity().equals( other.identity() );
    }

}
