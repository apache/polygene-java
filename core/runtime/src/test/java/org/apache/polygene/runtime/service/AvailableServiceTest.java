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

package org.apache.polygene.runtime.service;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.configuration.ConfigurationComposite;
import org.apache.polygene.api.configuration.Enabled;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.Availability;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.service.qualifier.Available;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * JAVADOC
 */
public class AvailableServiceTest
{
    @Test
    public void givenAvailableServiceWhenCheckServiceReferenceThenReturnTrue()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( TestServiceComposite1.class );
            }
        };

        ServiceReference<TestServiceComposite1> serviceRef = assembler.module()
            .findService( TestServiceComposite1.class );

        assertThat( "service is available", serviceRef.isAvailable(), equalTo( true ) );
    }

    @Test
    public void givenEnablableServiceWhenCheckAvailableThenReturnEnabledStatus()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( TestServiceComposite2.class );
                module.entities( TestServiceConfiguration.class );

                new EntityTestAssembler().assemble( module );
            }
        };

        ServiceReference<TestServiceComposite2> serviceRef = assembler.module()
            .findService( TestServiceComposite2.class );

        assertThat( "service is unavailable", serviceRef.isAvailable(), equalTo( false ) );

        serviceRef.get().get().enabled().set( true );
        serviceRef.get().save();

        assertThat( "service is available", serviceRef.isAvailable(), equalTo( true ) );
    }

    @Test
    public void givenEnablableServiceWhenInjectWithAvailableQualifierThenInjectCorrectly()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( TestObject.class );
                module.services( TestServiceComposite2.class );
                module.entities( TestServiceConfiguration.class );

                new EntityTestAssembler().assemble( module );
            }
        };

        TestObject object = assembler.module().newObject( TestObject.class );

        assertThat( "service is unavailable", object.getService(), nullValue() );

        ServiceReference<TestServiceComposite2> serviceRef = assembler.module()
            .findService( TestServiceComposite2.class );
        serviceRef.get().get().enabled().set( true );
        serviceRef.get().save();

        object = assembler.module().newObject( TestObject.class );
        assertThat( "service is available", object.getService(), notNullValue() );
    }

    // This service has to be asked for availability
    @Mixins( TestMixin1.class )
    public interface TestServiceComposite1
        extends TestService, Availability, ServiceComposite
    {
    }

    // This service has availability set through configuration
    @Mixins( TestMixin2.class )
    public interface TestServiceComposite2
        extends TestService, Configuration<TestServiceConfiguration>, ServiceComposite
    {
    }

    public interface TestServiceConfiguration
        extends Enabled, ConfigurationComposite
    {
    }

    public interface TestService
    {
        String test();
    }

    public static class TestMixin1
        implements TestService, Availability
    {
        public String test()
        {
            return "mixin1";
        }

        public boolean isAvailable()
        {
            return true;
        }
    }

    public static class TestMixin2
        implements TestService
    {
        @This
        Configuration<TestServiceConfiguration> config;

        public String test()
        {
            return "mixin2";
        }
    }

    public static class TestObject
    {
        @Service
        @Optional
        @Available
        TestService service;

        public TestService getService()
        {
            return service;
        }
    }
}