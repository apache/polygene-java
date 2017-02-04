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
package org.apache.polygene.runtime.service;

import java.util.stream.Stream;
import org.apache.polygene.api.activation.Activation;
import org.apache.polygene.api.activation.ActivationEventListener;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.composite.ModelDescriptor;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.service.ServiceImporterException;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.service.ServiceUnavailableException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.runtime.activation.ActivationDelegate;

/**
 * Implementation of ServiceReference. This manages the reference to the imported service.
 * <p>
 * Whenever the service is requested it is returned directly to the client. That means that
 * to handle service passivation and unavailability correctly, any proxying must be done in the
 * service importer.
 * </p>
 *
 * @param <T> Service Type
 */
public final class ImportedServiceReferenceInstance<T>
    implements ServiceReference<T>, Activation
{
    private volatile ImportedServiceInstance<T> serviceInstance;
    private T instance;
    private final ModuleDescriptor module;
    private final ImportedServiceModel serviceModel;
    private final ActivationDelegate activation = new ActivationDelegate( this );
    private boolean active = false;

    public ImportedServiceReferenceInstance( ImportedServiceModel serviceModel, ModuleDescriptor module )
    {
        this.module = module;
        this.serviceModel = serviceModel;
    }

    @Override
    public Identity identity()
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
        return getInstance();
    }

    public ImportedServiceModel serviceDescriptor()
    {
        return serviceModel;
    }

    @Override
    public void activate()
        throws ActivationException
    {
        if( serviceModel.isImportOnStartup() )
        {
            getInstance();
        }
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        if( serviceInstance != null )
        {
            try
            {
                activation.passivate( () -> active = false );
            }
            finally
            {
                serviceInstance = null;
                active = false;
            }
        }
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    public boolean isAvailable()
    {
        try
        {
            getInstance();
            return serviceInstance.isAvailable();
        }
        catch( ServiceImporterException ex )
        {
            return false;
        }
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

    private T getInstance()
        throws ServiceImporterException
    {
        // DCL that works with Java 1.5 volatile semantics
        if( serviceInstance == null )
        {
            synchronized( this )
            {
                if( serviceInstance == null )
                {
                    serviceInstance = serviceModel.importInstance( module );
                    instance = serviceInstance.instance();

                    try
                    {
                        activation.activate(
                            serviceModel.newActivatorsInstance( module ),
                            serviceInstance, () -> {
                                active = true;
                            }
                        );
                    }
                    catch( Exception e )
                    {
                        serviceInstance = null;
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
        return serviceModel.identity() + ", active=" + isActive() + ", module='" + serviceModel.moduleName() + "'";
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
    public boolean equals( Object obj )
    {
        if( obj == null )
        {
            return false;
        }
        if( getClass() != obj.getClass() )
        {
            return false;
        }
        final ServiceReference other = (ServiceReference) obj;
        return identity().equals( other.identity() );
    }
}
