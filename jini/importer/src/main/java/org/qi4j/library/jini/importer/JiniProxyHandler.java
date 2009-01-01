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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.io.IOException;
import org.qi4j.api.service.ImportedServiceDescriptor;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceItemFilter;
import net.jini.core.lookup.ServiceTemplate;

public class JiniProxyHandler
    implements InvocationHandler
{

    JiniProxyHandler( ImportedServiceDescriptor descriptor )
        throws IOException
    {
        ServiceDiscoveryManager sdm = new ServiceDiscoveryManager( null, null );
        ServiceDiscoveryListener listener = new JiniStatusService();
        ServiceItemFilter filter = null;
        ServiceTemplate template = null;
        sdm.createLookupCache( template, filter, listener );
    }

    public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
