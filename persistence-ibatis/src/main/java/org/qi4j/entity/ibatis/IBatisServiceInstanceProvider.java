/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis;

import java.util.Map;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.composite.scope.Adapt;
import org.qi4j.service.ActivationStatus;
import org.qi4j.service.ActivationStatusChange;
import org.qi4j.spi.service.ServiceInstance;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.service.ServiceProviderException;
import org.qi4j.spi.structure.ServiceDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class IBatisServiceInstanceProvider
    implements ServiceInstanceProvider
{
    private ServiceDescriptor descriptor;
    private ServiceInstance serviceInstance;

    /**
     * Construct an instance of {@code IBatisServiceInstanceProvider}.
     *
     * @since 0.1.0
     */
    public IBatisServiceInstanceProvider()
    {
        descriptor = null;
        serviceInstance = null;
    }

    /**
     * Initialize this {@code IBatisServiceInstanceProvider}.
     *
     * @param aDescriptor The descriptor.
     * @throws IllegalArgumentException Thrown if the specified argument is {@code null}.
     * @since 0.1.0
     */
    public final void init( @Adapt ServiceDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", descriptor );

        // Perform validation of service infos to ensure that on later stage it's possible to instantiate service.
        Map<Class, Object> serviceInfos = aDescriptor.getServiceInfos();
        Object serviceInfo = serviceInfos.get( IBatisEntityStoreServiceInfo.class );
        if( serviceInfo == null )
        {
            String infoClassName = IBatisEntityStoreServiceInfo.class.getName();
            String message = "[aDescriptor] must have service info of type [" + infoClassName + "]";
            throw new IllegalArgumentException( message );
        }

        descriptor = aDescriptor;
    }

    /**
     * TODO: What is the relation between activation status change and this method.
     *
     * @return The service instance.
     * @throws ServiceProviderException TODO Need to know why this is thrown.
     * @since 0.1.0
     */
    public final ServiceInstance getInstance()
        throws ServiceProviderException
    {
        if( serviceInstance == null )
        {
            String message = "The module that contains this service has not passed starting state.";
            throw new ServiceProviderException( message );
        }

        return serviceInstance;
    }

    public final void releaseInstance( ServiceInstance instance )
        throws Exception
    {
        // TODO: It seems that this is invoked on every EntitySessionInstance#complete()
        // TODO: I'm not sure what is the symantic of releaseInstance.
    }

    /**
     * Perform clean up, initialization when activation of the module that contains this changed.
     *
     * @param aChange The activation status change. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if the specified {@code aChange} argument is {@code null}.
     * @throws Exception
     */
    public final void onActivationStatusChange( ActivationStatusChange aChange )
        throws Exception
    {
        validateNotNull( "aChange", aChange );

        ActivationStatus newStatus = aChange.getNewStatus();
        switch( newStatus )
        {
        case STARTING:
            Map<Class, Object> serviceInfos = descriptor.getServiceInfos();
            IBatisEntityStore entityStore = new IBatisEntityStore( descriptor );
            serviceInstance = new ServiceInstance( entityStore, this, serviceInfos );
            break;

        case STOPPING:
            IBatisEntityStore instance = (IBatisEntityStore) serviceInstance.getInstance();
            instance.passivate();
            serviceInstance = null;
            break;
        }
    }
}
