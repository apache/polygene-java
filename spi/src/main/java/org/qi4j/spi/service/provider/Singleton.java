/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.service.provider;

import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Adapt;
import org.qi4j.composite.scope.Structure;
import org.qi4j.service.Activatable;
import org.qi4j.service.ActivationStatus;
import org.qi4j.service.ActivationStatusChange;
import org.qi4j.spi.service.ServiceInstance;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.service.ServiceProviderException;
import org.qi4j.spi.structure.ServiceDescriptor;

/**
 * TODO
 */
public final class Singleton
    implements ServiceInstanceProvider
{
    private @Structure CompositeBuilderFactory cbf;
    private @Structure ObjectBuilderFactory obf;

    private ServiceInstance instance;
    private ServiceDescriptor descriptor;

    public void init( @Adapt ServiceDescriptor descriptor )
    {
        this.descriptor = descriptor;
    }

    public ServiceInstance getInstance()
        throws ServiceProviderException
    {
        return instance;
    }

    public void releaseInstance( ServiceInstance instance )
        throws Exception
    {
    }

    public void onActivationStatusChange( ActivationStatusChange change )
        throws Exception
    {
        // When module starts, create instance eagerly
        if( change.getNewStatus() == ActivationStatus.STARTING )
        {
            Object serviceInstance;
            if( Composite.class.isAssignableFrom( descriptor.getServiceType() ) )
            {
                serviceInstance = cbf.newComposite( descriptor.getServiceType() );
            }
            else
            {
                serviceInstance = obf.newObject( descriptor.getServiceType() );
            }

            if( serviceInstance instanceof Activatable )
            {
                ( (Activatable) serviceInstance ).activate();
            }

            instance = new ServiceInstance( serviceInstance, this, descriptor.getServiceInfos() );
        }
        // When module stops, discard reference to object
        else if( change.getNewStatus() == ActivationStatus.STOPPING )
        {
            if( instance instanceof Activatable )
            {
                ( (Activatable) instance ).passivate();
            }
            instance = null;
        }
    }
}
