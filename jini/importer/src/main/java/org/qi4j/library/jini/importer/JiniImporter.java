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

import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporterException;
import java.lang.reflect.Proxy;

public class JiniImporter
    implements ServiceImporter
{
    /**
     * Import a service from Jini by looking creating a Proxy which holds the Lookup cache to the wanted Jini service.
     *
     * @param serviceDescriptor The service descriptor.
     * @return a service instance
     * @throws ServiceImporterException
     */

    public Object importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        JiniProxyHandler handler = new JiniProxyHandler( serviceDescriptor );
        Class[] type = new Class[] { serviceDescriptor.type() };
        return Proxy.newProxyInstance( JiniImporter.class.getClassLoader(), type, handler );
    }
}
