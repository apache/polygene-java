/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2012-2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.service;

import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.ServiceUnavailableException;
import org.qi4j.api.structure.Module;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ServiceReference. This manages the reference to the imported service.
 * <p/>
 * Whenever the service is requested it is returned directly to the client. That means that
 * to handle service passivation and unavailability correctly, any proxying must be done in the
 * service importer.
 *
 * @param <T> Service Type
 */
public final class ImportedServiceReferenceInstance<T>
    implements ServiceReference<T>, Activation
{
    private volatile ImportedServiceInstance<T> serviceInstance;
    private T instance;
    private final Module module;
    private final ImportedServiceModel serviceModel;
    private final ActivationDelegate activation = new ActivationDelegate( this );
    private boolean active = false;

    public ImportedServiceReferenceInstance( ImportedServiceModel serviceModel, Module module )
    {
        this.module = module;
        this.serviceModel = serviceModel;
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
                activation.passivate( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        active = false;
                    }
                } );
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
            LoggerFactory.getLogger( getClass() )
                .warn( "Imported service throwed an exception on isAvailable(), will return false.", ex );
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
                    serviceInstance = serviceModel.<T>importInstance( module );
                    instance = serviceInstance.instance();

                    try
                    {
                        activation.activate( serviceModel.newActivatorsInstance( module ), serviceInstance, new Runnable()
                        {
                            @Override
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
