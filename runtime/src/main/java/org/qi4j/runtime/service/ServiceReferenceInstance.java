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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.service.Activator;
import org.qi4j.api.structure.Module;

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
    private volatile ServiceInstance<T> serviceInstance;
    private Object instance;
    private final T serviceProxy;
    private final Module module;
    private final ServiceModel serviceModel;
    private final Activator activator = new Activator();

    public ServiceReferenceInstance( ServiceModel serviceModel, Module module )
    {
        this.module = module;
        this.serviceModel = serviceModel;

        serviceProxy = newProxy();
    }

    public String identity()
    {
        return serviceModel.identity();
    }

    public <K> K metaInfo( Class<K> infoType )
    {
        return serviceModel.metaInfo().get( infoType );
    }

    public synchronized T get()
    {
        return serviceProxy;
    }

    public boolean isActive()
    {
        return serviceInstance != null;
    }

    public void activate()
        throws Exception
    {
        if( serviceModel.isInstantiateOnStartup() )
        {
            getInstance();
        }
    }

    public void passivate()
        throws Exception
    {
        if( serviceInstance != null )
        {
            activator.passivate();
            serviceInstance = null;
        }
    }

    private Object getInstance()
        throws ServiceImporterException
    {
        // DCL that works with Java 1.5 volatile semantics
        if( serviceInstance == null )
        {
            synchronized( this )
            {
                if( serviceInstance == null )
                {
                    serviceInstance = (ServiceInstance<T>) serviceModel.newInstance( module );
                    T providedInstance = serviceInstance.instance();

                    if( providedInstance instanceof Activatable )
                    {
                        try
                        {
                            activator.activate( (Activatable) providedInstance );
                        }
                        catch( Exception e )
                        {
                            serviceInstance = null;
                            throw new ServiceImporterException( e );
                        }
                    }

                    if( providedInstance instanceof Composite )
                    {
                        InvocationHandler handler = Proxy.getInvocationHandler( providedInstance );
                        if( handler instanceof CompositeInstance )
                        {
                            instance = handler;
                        }
                        else
                        {
                            instance = providedInstance;
                        }

                    }
                    else
                    {
                        instance = providedInstance;
                    }
                }
            }
        }

        return instance;
    }

    @Override public String toString()
    {
        return serviceModel.identity() + ", active=" + ( serviceInstance != null ) + ", module='" + serviceModel.moduleName() + "'";
    }


    public T newProxy()
    {
        return (T) serviceModel.newProxy( new ServiceReferenceInstance.ServiceInvocationHandler() );
    }

    public final class ServiceInvocationHandler
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            if( method.getName().equals( "toString" ) )
            {
                return serviceModel.toString();
            }
            Object instance = getInstance();

            if( instance instanceof InvocationHandler )
            {
                InvocationHandler handler = (InvocationHandler) instance;
                return handler.invoke( serviceInstance.instance(), method, objects );
            }
            else
            {
                try
                {
                    return method.invoke( instance, objects );
                }
                catch( InvocationTargetException e )
                {
                    throw e.getTargetException();
                }
            }
        }

        @Override public String toString()
        {
            return serviceModel.toString();
        }
    }
}
