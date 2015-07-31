/*
 * Copyright 2012 Paul Merlin.
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
package org.apache.zest.api.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractQi4jTest;
import org.apache.zest.test.EntityTestAssembler;

public class DeclareConfigurationDefaultsTest
        extends AbstractQi4jTest
{

    @Mixins( FooServiceMixin.class )
    public static interface FooServiceComposite
            extends ServiceComposite
    {

        String configuredFoo();

    }

    public static abstract class FooServiceMixin
            implements FooServiceComposite
    {

        @This
        private Configuration<FooConfigurationComposite> config;

        public String configuredFoo()
        {
            return config.get().foo().get();
        }

    }

    public static interface FooConfigurationComposite
            extends ConfigurationComposite
    {

        Property<String> foo();

    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.services( FooServiceComposite.class ).identifiedBy( "bazar" );
        module.entities( FooConfigurationComposite.class );
        new EntityTestAssembler().assemble( module );
        FooConfigurationComposite config = module.forMixin( FooConfigurationComposite.class ).declareDefaults();
        config.foo().set( "bar" );
    }

    @Test
    public void testConfigurationDefaults()
    {
        FooServiceComposite fooService = module.findService( FooServiceComposite.class ).get();
        Assert.assertEquals( "bar", fooService.configuredFoo() );
    }

}