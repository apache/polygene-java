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

package org.apache.polygene.test.entity;

import org.junit.Test;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.value.ValueSerialization;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.memory.MemoryEntityStoreService;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public abstract class AbstractConfigurationDeserializationTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
//        ModuleAssembly storageModule = module.layer().module( "storage" );
        @SuppressWarnings( "UnnecessaryLocalVariable" )
        ModuleAssembly storageModule = module; // Disable the more complex set up. The entire value serialization has gotten the deserialization type lookup problem wrong.
        module.configurations( ConfigSerializationConfig.class );
        module.values( Host.class );
        module.services( MyService.class ).identifiedBy( "configtest" );
        storageModule.services( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
        storageModule.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
    }

    @Test
    public void givenServiceWhenInitializingExpectCorrectDeserialization()
    {
        ServiceReference<MyService> ref = module.instance().findService( MyService.class );
        assertThat( ref, notNullValue() );
        assertThat( ref.isAvailable(), equalTo( true ) );
        MyService myService = ref.get();
        assertThat( myService, notNullValue() );
        assertThat( myService.name(), equalTo( "main" ) );
        assertThat( myService.hostIp(), equalTo( "12.23.34.45" ) );
        assertThat( myService.hostPort(), equalTo( 1234 ) );
    }

    @Mixins( MyServiceMixin.class )
    public interface MyService
    {

        String hostIp();

        Integer hostPort();

        String name();
    }

    public static class MyServiceMixin
        implements MyService
    {

        @This
        private Configuration<ConfigSerializationConfig> config;

        @Override
        public String hostIp()
        {
            return config.get().host().get().ip().get();
        }

        @Override
        public Integer hostPort()
        {
            return config.get().host().get().port().get();
        }

        @Override
        public String name()
        {
            return config.get().name().get();
        }
    }

    public interface ConfigSerializationConfig extends HasIdentity
    {
        Property<String> name();

        Property<Host> host();
    }

    public interface Host
    {
        Property<String> ip();

        Property<Integer> port();
    }
}
