/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.jini.importer;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.observations.Observable;

public class JiniStatusMixin
    implements JiniStatus, ServiceDiscoveryListener
{
    @This Observable<JiniServiceObservation> observable;

    private WeakHashMap<ServiceID, Object> serviceIds;
    private WeakHashMap<Class, Set<Object>> services;

    public JiniStatusMixin()
    {
        services = new WeakHashMap<Class, Set<Object>>();
        serviceIds = new WeakHashMap<ServiceID, Object>();
    }

    public boolean isAvailable( ServiceID service )
    {
        return serviceIds.containsKey( service );
    }

    public boolean isAvailable( Class type )
    {
        return services.containsKey( type );
    }

    public void serviceAdded( ServiceDiscoveryEvent serviceDiscoveryEvent )
    {
        ServiceItem item = serviceDiscoveryEvent.getPostEventServiceItem();
        Object service = item.service;
        Class[] types = item.service.getClass().getClasses();
        ServiceID id = item.serviceID;
        serviceIds.put( id, service );
        for( Class type : types )
        {
            Set<Object> set = services.get( type );
            if( set == null )
            {
                set = new HashSet<Object>();
                services.put( type, set );
            }
            set.add( service );
        }
    }

    public void serviceRemoved( ServiceDiscoveryEvent serviceDiscoveryEvent )
    {
        ServiceItem item = serviceDiscoveryEvent.getPreEventServiceItem();
        Object service = item.service;
        Class[] types = item.service.getClass().getClasses();
        ServiceID id = item.serviceID;
        serviceIds.remove( id );
        for( Class type : types )
        {
            Set<Object> set = services.get( type );
            if( set != null )
            {
                set.remove( service );
                if( set.size() == 0 )
                {
                    services.remove( type );
                }
            }
        }
    }

    public void serviceChanged( ServiceDiscoveryEvent serviceDiscoveryEvent )
    {
    }
}
