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
import java.lang.reflect.Method;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.service.Activator;

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
    private volatile CompositeInstance instance;
    private final T serviceProxy;
    private final ModuleInstance module;
    private final ServiceModel serviceModel;
    private final Activator activator = new Activator();

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

    public MetaInfo metaInfo()
    {
        return serviceModel.metaInfo();
    }

    public synchronized T get()
    {
        return serviceProxy;
    }

    public boolean isActive()
    {
        return instance != null;
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
        if( instance != null )
        {
            activator.passivate();
            instance = null;
        }
    }

    private CompositeInstance getInstance()
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

                    if( instance.<T>proxy() instanceof Activatable )
                    {
                        try
                        {
                            activator.activate( (Activatable) instance.proxy() );
                        }
                        catch( Exception e )
                        {
                            instance = null;
                            throw new ServiceImporterException( e );
                        }
                    }
                }
            }
        }

        return instance;
    }

    @Override public String toString()
    {
        return serviceModel.identity() + ", active=" + isActive() + ", module='" + serviceModel.moduleName() + "'";
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
            CompositeInstance instance = getInstance();

            return instance.invoke( object, method, objects );
        }

        @Override public String toString()
        {
            return serviceModel.toString();
        }
    }
}
