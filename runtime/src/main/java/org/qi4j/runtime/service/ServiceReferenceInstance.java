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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.composite.Composite;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.runtime.composite.CompositeInstance;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceInstanceProviderException;
import org.qi4j.service.ServiceReference;
import org.qi4j.spi.property.GenericPropertyInfo;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.service.ServiceInstance;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.structure.ServiceDescriptor;

/**
 * TODO
 */
public final class ServiceReferenceInstance<T>
    implements ServiceReference<T>, Activatable
{
    private ServiceDescriptor serviceDescriptor;
    private ServiceInstanceProvider serviceInstanceProvider;
    private ImmutableProperty identity;

    private volatile ServiceInstance<T> serviceInstance;
    private Object instance;
    private T serviceProxy;
    private int referenceCounter = 0;

    public ServiceReferenceInstance( ServiceDescriptor serviceDescriptor, ServiceInstanceProvider serviceInstanceProvider )
    {
        this.serviceDescriptor = serviceDescriptor;
        this.serviceInstanceProvider = serviceInstanceProvider;
        identity = new ImmutablePropertyInstance( new GenericPropertyInfo( PropertyModel.getQualifiedName( ServiceReference.class, "identity" ) ), serviceDescriptor.getIdentity() );

        serviceProxy = (T) Proxy.newProxyInstance( serviceDescriptor.getServiceType().getClassLoader(), new Class[]{ serviceDescriptor.getServiceType() }, new ServiceInvocationHandler() );
    }

    public ImmutableProperty identity()
    {
        return identity;
    }

    public <K extends Serializable> K getServiceInfo( Class<K> infoType )
    {
        return infoType.cast( serviceDescriptor.getServiceInfos().get( infoType ) );
    }

    public <K extends Serializable> void setServiceInfo( Class<K> infoType, K value )
    {
        // TODO Not thread-safe
        serviceDescriptor.getServiceInfos().put( infoType, value );
    }

    public synchronized T getService()
    {
        referenceCounter++;
        return serviceProxy;
    }

    public synchronized void releaseService()
        throws IllegalStateException
    {
        if( referenceCounter == 0 )
        {
            throw new IllegalStateException( "All references already released" );
        }

        referenceCounter--;
    }

    public void activate() throws Exception
    {
        if( serviceDescriptor.isActivateOnStartup() )
        {
            T instance = (T) serviceInstanceProvider.newInstance( serviceDescriptor );
            serviceInstance = new ServiceInstance<T>( instance, serviceInstanceProvider, serviceDescriptor );

            if( serviceInstance.getInstance() instanceof Activatable )
            {
                ( (Activatable) serviceInstance.getInstance() ).activate();
            }
        }
    }

    public void passivate() throws Exception
    {
        if( serviceInstance != null )
        {
            // Passivate the instance
            T instance = serviceInstance.getInstance();
            if( instance instanceof Activatable )
            {
                ( (Activatable) instance ).passivate();
            }

            // Release the instance
            try
            {
                serviceInstanceProvider.releaseInstance( serviceInstance );
            }
            finally
            {
                serviceInstance = null;
            }
        }
    }

    public int getReferenceCounter()
    {
        return referenceCounter;
    }

    public ServiceDescriptor getServiceDescriptor()
    {
        return serviceDescriptor;
    }

    private Object getInstance()
        throws ServiceInstanceProviderException
    {
        // DCL that works with Java 1.5 volatile semantics
        if( serviceInstance == null )
        {
            synchronized( this )
            {
                if( serviceInstance == null )
                {
                    T providedInstance = (T) serviceInstanceProvider.newInstance( serviceDescriptor );
                    serviceInstance = new ServiceInstance<T>( providedInstance, serviceInstanceProvider, serviceDescriptor );

                    if( providedInstance instanceof Activatable )
                    {
                        try
                        {
                            ( (Activatable) providedInstance ).activate();
                        }
                        catch( Exception e )
                        {
                            serviceInstance = null;
                            throw new ServiceInstanceProviderException( e );
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

    public final class ServiceInvocationHandler
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            Object instance = getInstance();

            if( instance instanceof InvocationHandler )
            {
                InvocationHandler handler = (InvocationHandler) instance;
                return handler.invoke( instance, method, objects );
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
    }
}
