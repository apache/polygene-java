/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright 2012, Paul Merlin.
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

import org.qi4j.api.event.ActivationEvent;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.ServiceUnavailableException;
import org.qi4j.api.structure.Module;
import org.qi4j.runtime.activation.ActivationHandler;
import org.qi4j.runtime.structure.ActivationEventListenerSupport;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ServiceReference. This manages the reference to the imported service.
 * <p/>
 * Whenever the service is requested it is returned directly to the client. That means that
 * to handle service passivation and unavailability correctly, any proxying must be done in the
 * service importer.
 */
public final class ImportedServiceReferenceInstance<T>
    implements ServiceReference<T>, Activatable
{
    private volatile ImportedServiceInstance<T> serviceInstance;
    private T instance;
    private final Module module;
    private final ImportedServiceModel serviceModel;
    private final ActivationHandler activationHandler = new ActivationHandler();
    private final ActivationEventListenerSupport eventListenerSupport = new ActivationEventListenerSupport();
    private boolean active = false;

    public ImportedServiceReferenceInstance( ImportedServiceModel serviceModel, Module module )
    {
        this.module = module;
        this.serviceModel = serviceModel;
    }

    public String identity()
    {
        return serviceModel.identity();
    }

    @Override
    public Iterable<Class<?>> types()
    {
        return serviceModel.types();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return serviceModel.metaInfo( infoType );
    }

    public synchronized T get()
    {
        return getInstance();
    }

    public ImportedServiceModel serviceDescriptor()
    {
        return serviceModel;
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
        if( serviceModel.isImportOnStartup() )
        {
            getInstance();
        }
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    public void passivate()
            throws Exception
    {
        if( serviceInstance != null )
        {
            eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
            try {
                activationHandler.passivate( this, new Runnable()
                {

                    public void run()
                    {
                        active = false;
                    }

                } );
            } finally {
                serviceInstance = null;
                active = false;
            }
            eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
        }
    }

    public boolean isActive()
    {
        return active;
    }

    public boolean isAvailable()
    {
        try
        {
            getInstance();
            return serviceInstance.isAvailable();
        }
        catch( ServiceImporterException e )
        {
            LoggerFactory.getLogger( getClass() ).warn( "Imported service throwed an exception on isAvailable(), will return false.", e );
            return false;
        }
    }

    public Module module()
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
                    serviceInstance = (ImportedServiceInstance<T>) serviceModel.<T>importInstance( module );
                    instance = serviceInstance.instance();

                    try
                    {
                        activationHandler.activate( this, serviceModel.newActivatorsInstance(), serviceInstance, new Runnable()
                        {

                            public void run()
                            {
                                active = true;
                            }

                        } );
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
}
