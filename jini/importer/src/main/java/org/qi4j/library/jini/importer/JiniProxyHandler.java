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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryManagement;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;
import org.qi4j.api.service.ImportedServiceDescriptor;

public class JiniProxyHandler
    implements InvocationHandler, ServiceDiscoveryListener
{
    private JiniStatusService statusService;
    private LookupCache lookupCache;
    private Object jiniService;

    JiniProxyHandler( ImportedServiceDescriptor descriptor, JiniStatusService statusService )
        throws IOException
    {
        this.statusService = statusService;
        LeaseRenewalManager leaseRenewal = null;
        DiscoveryManagement discoveryManagement = null;
        ServiceDiscoveryManager sdm = new ServiceDiscoveryManager( discoveryManagement, leaseRenewal );
        ServiceItemFilter filter = null;
        Entry[] entries = null;             // Lookup all, regardless of entries.
        ServiceID serviceId = null;         // Lookup all, regardless of serviceId.
        Class[] type = new Class[]{ descriptor.type() };
        ServiceTemplate template = new ServiceTemplate( serviceId, type, entries );
        lookupCache = sdm.createLookupCache( template, filter, this );
        lookupCache.addListener( this );
    }

    public Object invoke( Object o, Method method, Object[] args )
        throws Throwable
    {
        synchronized( this )
        {
            if( jiniService != null )
            {
                return method.invoke( jiniService, args );
            }
            jiniService = lookupCache.lookup( null ).service;
            if( jiniService != null )
            {
                return method.invoke( jiniService, args );
            }
            // TODO: Handle not available!!!
            return null;
        }
    }

    public void serviceAdded( ServiceDiscoveryEvent serviceDiscoveryEvent )
    {
        synchronized( this )
        {
            ServiceItem item = serviceDiscoveryEvent.getPostEventServiceItem();
            jiniService = item.service;
        }
        if( statusService != null )
        {
            statusService.serviceAdded( serviceDiscoveryEvent );
        }
    }

    public void serviceRemoved( ServiceDiscoveryEvent serviceDiscoveryEvent )
    {
        synchronized( this )
        {
            ServiceItem item = serviceDiscoveryEvent.getPostEventServiceItem();
            if( item.service.equals( jiniService ) )
            {
                jiniService = null;
            }
        }
        if( statusService != null )
        {
            statusService.serviceRemoved( serviceDiscoveryEvent );
        }
    }

    public void serviceChanged( ServiceDiscoveryEvent serviceDiscoveryEvent )
    {
        if( statusService != null )
        {
            statusService.serviceChanged( serviceDiscoveryEvent );
        }
    }
}
