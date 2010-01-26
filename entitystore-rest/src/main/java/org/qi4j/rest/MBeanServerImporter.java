/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest;

import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;

/**
 * JAVADOC
 */
public class MBeanServerImporter
    implements ServiceImporter
{
    public Object importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        List<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer( null );
        if( mbeanServers.size() > 0 )
        {
            return mbeanServers.get( 0 );
        }
        else
        {
            return MBeanServerFactory.createMBeanServer( "StreamFlow" );
        }
    }

    public boolean isActive( Object instance )
    {
        return true;
    }
}
