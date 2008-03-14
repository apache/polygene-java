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

package org.qi4j.service;

import java.io.Serializable;
import junit.framework.TestCase;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.Service;

/**
 * TODO
 */
public class ServiceInjectionTest
    extends TestCase
{
    public void testInjectService()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices( MyServiceComposite.class ).setServiceInfo( ServiceName.class, new ServiceName( "Foo" ) );
                module.addServices( MyServiceComposite.class ).setServiceInfo( ServiceName.class, new ServiceName( "Bar" ) );
                module.addObjects( ServiceUser.class );
            }
        };

        ServiceUser user = assembly.getObjectBuilderFactory().newObjectBuilder( ServiceUser.class ).newInstance();

        assertEquals( "X", user.testSingle() );
        assertEquals( "XX", user.testIterable() );
        assertEquals( "FooX", user.testServiceReference() );
        assertEquals( "FooXBarX", user.testIterableServiceReferences() );
    }

    @Mixins( MyServiceMixin.class )
    public static interface MyServiceComposite
        extends MyService, ServiceComposite
    {
    }

    public static interface MyService
    {
        String doStuff();
    }

    public static class MyServiceMixin
        implements MyService
    {

        public String doStuff()
        {
            return "X";
        }
    }

    public static class ServiceUser
    {
        @Service MyService service;
        @Service Iterable<MyService> services;
        @Service ServiceReference<MyService> serviceRef;
        @Service Iterable<ServiceReference<MyService>> serviceRefs;

        public String testSingle()
        {
            return service.doStuff();
        }

        public String testIterable()
        {
            String str = "";
            for( MyService myService : services )
            {
                str += myService.doStuff();
            }
            return str;
        }

        public String testServiceReference()
            throws ServiceInstanceProviderException
        {
            ServiceName info = serviceRef.getServiceInfo( ServiceName.class );
            return info.getName() + serviceRef.getService().doStuff();
        }

        public String testIterableServiceReferences()
            throws ServiceInstanceProviderException
        {
            String str = "";
            for( ServiceReference<MyService> serviceReference : serviceRefs )
            {
                str += serviceReference.getServiceInfo( ServiceName.class ).getName();
                str += serviceReference.getService().doStuff();
            }
            return str;
        }
    }

    public static class ServiceName
        implements Serializable
    {
        private String name;

        public ServiceName( String name )
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }
}
