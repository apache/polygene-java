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
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceProviderException;
import org.qi4j.service.ServiceReference;
import org.qi4j.spi.service.ServiceInstance;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.structure.ServiceDescriptor;

/**
 * TODO
 */
public class ServiceReferenceInstance<T>
    implements ServiceReference<T>, Activatable
{
    private ServiceDescriptor serviceDescriptor;
    private ServiceInstanceProvider serviceInstanceProvider;

    private ServiceInstance<T> serviceInstance;
    private int referenceCounter = 0;

    public ServiceReferenceInstance( ServiceDescriptor serviceDescriptor, ServiceInstanceProvider serviceInstanceProvider )
    {
        this.serviceDescriptor = serviceDescriptor;
        this.serviceInstanceProvider = serviceInstanceProvider;
    }

    public <K extends Serializable> K getServiceInfo( Class<K> infoType )
    {
        return infoType.cast( serviceDescriptor.getServiceInfos().get( infoType ) );
    }

    public <K extends Serializable> void setServiceInfo( Class<K> infoType, K value )
    {
        serviceDescriptor.getServiceInfos().put( infoType, value );
    }

    public synchronized T getInstance()
        throws ServiceProviderException
    {
        if( serviceInstance == null )
        {
            T instance = (T) serviceInstanceProvider.newInstance( serviceDescriptor );
            serviceInstance = new ServiceInstance<T>( instance, serviceInstanceProvider, serviceDescriptor );

            if( serviceInstance.getInstance() instanceof Activatable )
            {
                try
                {
                    ( (Activatable) serviceInstance.getInstance() ).activate();
                }
                catch( Exception e )
                {
                    serviceInstance = null;
                    throw new ServiceProviderException( e );
                }
            }
        }

        referenceCounter++;

        return serviceInstance.getInstance();
    }

    public synchronized void release()
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
            // Don't allow passivation if there are any active references
            if( referenceCounter > 0 )
            {
                throw new IllegalStateException( "Cannot passivate a service instance of type " + serviceDescriptor.getServiceType() + " which still has " + referenceCounter + " active references" );
            }

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
}
