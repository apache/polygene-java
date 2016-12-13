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
package org.apache.polygene.runtime.instantiation;

import org.junit.Assert;
import org.junit.Test;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;

public class ServiceInstantiationTests
    extends AbstractPolygeneTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( MyConfiguration.class );
        module.services( My.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void whenCreatingServiceCompositeGivenAServiceCompositeThenSucceed()
        throws Exception
    {
        ServiceReference<My> service = serviceFinder.findService( My.class );
        Assert.assertEquals( "HabbaZout", service.get().doSomething() );
    }

    @Mixins( MyMixin.class )
    public interface My
    {
        String doSomething();
    }

    public interface MyConfiguration
    {
        Property<String> data();
    }

    public static class MyMixin
        implements My
    {
        @This
        Configuration<MyConfiguration> config;

        @Override
        public String doSomething()
        {
            return config.get().data().get();
        }
    }
}