/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.runtime.service;

import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.apache.zest.api.service.qualifier.ServiceQualifier.firstService;
import static org.apache.zest.api.service.qualifier.ServiceQualifier.withId;

/**
 * JAVADOC
 */
public class ServiceIdSelectorTest
{
    @Test
    public void givenManyServicesWhenInjectServiceThenGetSelectedOne()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( ServiceConsumer.class );
                module.services( TestServiceComposite1.class,
                                 TestServiceComposite2.class );
            }
        };

        ObjectFactory obf = assembler.module();
        ServiceConsumer consumer = obf.newObject( ServiceConsumer.class, TestServiceComposite2.class.getSimpleName() );
        TestService service = consumer.getService();

        assertThat( "service is selected one", service.test(), equalTo( "mixin2" ) );
    }

    public static class ServiceConsumer
    {
        private TestService service;

        public ServiceConsumer( @Uses String serviceId, @Service Iterable<ServiceReference<TestService>> serviceRefs )
        {
            service = firstService( withId( serviceId ), serviceRefs );
        }

        public TestService getService()
        {
            return service;
        }
    }

    @Mixins( TestMixin1.class )
    public interface TestServiceComposite1
        extends TestService, ServiceComposite
    {
    }

    @Mixins( TestMixin2.class )
    public interface TestServiceComposite2
        extends TestService, ServiceComposite
    {
    }

    public interface TestService
    {
        String test();
    }

    public static class TestMixin1
        implements TestService
    {
        public String test()
        {
            return "mixin1";
        }
    }

    public static class TestMixin2
        implements TestService
    {
        public String test()
        {
            return "mixin2";
        }
    }
}