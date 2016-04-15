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
package org.apache.zest.runtime.service;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.apache.zest.api.activation.Activation;
import org.apache.zest.api.activation.ActivationEventListener;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.activation.PassivationException;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.CompositeDescriptor;
import org.apache.zest.api.composite.CompositeInstance;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.property.StateHolder;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.service.ServiceImporterException;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.service.ServiceUnavailableException;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.runtime.activation.ActivationDelegate;

/**
 * Implementation of ServiceReference.
 * <p>
 * This manages the actual instance of the service and implements the service Activation.
 * </p>
 * <p>
 * Whenever the service is requested a proxy is returned which points to this class. This means
 * that the instance can be passivated even though a client is holding on to a service proxy.
 * </p>
 * @param <T> Service Type
 */
public final class ServiceReferenceInstance<T>
    implements ServiceReference<T>, Activation, ModelDescriptor
{
    private volatile ServiceInstance instance;
    private final T serviceProxy;
    private final ModuleDescriptor module;
    private final ServiceModel serviceModel;
    private final ActivationDelegate activation = new ActivationDelegate( this );
    private boolean active = false;

    ServiceReferenceInstance( ServiceModel serviceModel, ModuleDescriptor module )
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
    public Stream<Class<?>> types()
    {
        return serviceModel.types();
    }

    @Override
    public <M> M metaInfo( Class<M> infoType )
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

    @Override
    public ModelDescriptor model()
    {
        return serviceModel;
    }

    public ModuleDescriptor module()
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
                activation.passivate( () -> active = false );
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
                        activation.activate( serviceModel.newActivatorsInstance( module ),
                                             instance,
                                             () -> active = true );
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

    @SuppressWarnings( "unchecked" )
    public T newProxy()
    {
        return (T) serviceModel.newProxy( new ServiceReferenceInstance.ServiceInvocationHandler() );
    }

    public ServiceDescriptor serviceDescriptor()
    {
        return serviceModel;
    }

    @Override
    public Visibility visibility()
    {
        return serviceModel.visibility();
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        return serviceModel.isAssignableTo( type );
    }

    public final class ServiceInvocationHandler
        implements CompositeInstance
    {
        @Override
        @SuppressWarnings( "unchecked" )
        public <P> P proxy()
        {
            return (P) ServiceReferenceInstance.this.get();
        }

        @Override
        public <P> P newProxy( Class<P> mixinType )
            throws IllegalArgumentException
        {
            return getInstance().newProxy( mixinType );
        }

        @Override
        public <M> M metaInfo( Class<M> infoType )
        {
            return ServiceReferenceInstance.this.metaInfo( infoType );
        }

        @Override
        public Stream<Class<?>> types()
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
                switch( method.getName() )
                {
                    case "toString":
                        return serviceModel.toString();
                    case "equals":
                        return objects[0] == object;
                    case "hashCode":
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
        public ModuleDescriptor module()
        {
            return module;
        }
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activation.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activation.deregisterActivationEventListener( listener );
    }

    @Override
    public int hashCode()
    {
        return identity().hashCode();
    }

    @Override
    @SuppressWarnings( "raw" )
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
