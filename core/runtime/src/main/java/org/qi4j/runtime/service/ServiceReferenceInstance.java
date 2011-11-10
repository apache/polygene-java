/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.event.ActivationEvent;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.*;
import org.qi4j.api.structure.Module;
import org.qi4j.runtime.structure.ActivationEventListenerSupport;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Implementation of ServiceReference. This manages the actual instance of the service
 * and implements the invocation of the Activatable interface on the service.
 * <p/>
 * Whenever the service is requested a proxy is returned which points to this class. This means
 * that the instance can be passivated even though a client is holding on to a service proxy.
 */
public final class ServiceReferenceInstance<T>
    implements ServiceReference<T>, Activatable
{
    private volatile ServiceInstance instance;
    private final T serviceProxy;
    private final ModuleInstance module;
    private final ServiceModel serviceModel;
    private final Activator activator = new Activator();
    private final ActivationEventListenerSupport eventListenerSupport = new ActivationEventListenerSupport();

    public ServiceReferenceInstance( ServiceModel serviceModel, ModuleInstance module )
    {
        this.module = module;
        this.serviceModel = serviceModel;

        serviceProxy = newProxy();
    }

    public String identity()
    {
        return serviceModel.identity();
    }

    @Override
    public Class<T> type()
    {
        return (Class<T>) serviceModel.type();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return serviceModel.metaInfo( infoType );
    }

    public synchronized T get()
    {
        return serviceProxy;
    }

    public boolean isActive()
    {
        return instance != null;
    }

    public boolean isAvailable()
    {
        return getInstance().isAvailable();
    }

    public Module module()
    {
        return module;
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.deregisterActivationEventListener( listener );
    }

    public void activate()
        throws Exception
    {
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATING ) );
        if( serviceModel.isInstantiateOnStartup() )
        {
            getInstance();
        }
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    public void passivate()
        throws Exception
    {
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
        if( instance != null )
        {
            activator.passivate();
            instance = null;
        }
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
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
                        activator.activate( instance );
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
        return serviceModel.identity() + ", active=" + isActive() + ", module='" + module.name() + "'";
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
        public Class<? extends Composite> type()
        {
            return (Class<Composite>) ServiceReferenceInstance.this.type();
        }

        @Override
        public CompositeDescriptor descriptor()
        {
            return ServiceReferenceInstance.this.serviceDescriptor();
        }

        @Override
        public Object invokeComposite( Method method, Object[] args ) throws Throwable
        {
            return getInstance().invokeComposite( method, args );
        }

        @Override
        public StateHolder state()
        {
            return getInstance().state();
        }

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

        public Module module()
        {
            return module;
        }
    }
}
