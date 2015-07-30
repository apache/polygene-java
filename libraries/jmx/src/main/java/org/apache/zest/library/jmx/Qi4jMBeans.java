/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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
package org.apache.zest.library.jmx;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.zest.functional.Iterables;
import org.apache.zest.functional.Specification;

/**
 * Helper for working with Zest MBeans.
 */
public class Qi4jMBeans
{

    public static ObjectName findServiceName( MBeanServer server, String applicationName, String serviceId )
            throws MalformedObjectNameException
    {
        return Iterables.first( Iterables.filter( new Specification<ObjectName>()
        {
            @Override
            public boolean satisfiedBy( ObjectName item )
            {
                return item.getKeyPropertyList().size() == 5;
            }

        }, server.queryNames( new ObjectName( "Zest:application=" + applicationName + ",*,service=" + serviceId ), null ) ) );
    }

}
