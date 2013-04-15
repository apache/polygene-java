/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.runtime.instantiation;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class ServiceInstantiationTests
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( MyConfigurationEntity.class );
        module.services( MyService.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void whenCreatingServiceCompositeGivenAServiceCompositeThenSucceed()
        throws Exception
    {
        ServiceReference<My> service = module.findService( My.class );
        Assert.assertEquals( "HabbaZout", service.get().doSomething() );
    }

    @Mixins( MyMixin.class )
    private interface MyService
        extends My, ServiceComposite
    {
    }

    public interface My
    {
        String doSomething();
    }

    public interface MyConfigurationEntity
        extends MyConfiguration, ConfigurationComposite
    {
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

        public String doSomething()
        {
            return config.get().data().get();
        }
    }
}