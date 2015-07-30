/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.api.configuration;

import org.junit.Test;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConfigurationTest extends AbstractZestTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MyService.class ).instantiateOnStartup();
        module.entities( MyConfig.class );
        module.values( PersonDetails.class, Address.class, City.class, Country.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testConfiguration()
        throws Exception
    {
        MyService service = module.findService( MyService.class ).get();
        PersonDetails details = service.details();
        assertThat(details.name().get(), equalTo( "Niclas" ) );
        assertThat(details.address().get().street1().get(), equalTo( "Henan Lu 555" ) );
        assertThat(details.address().get().street2().get(), equalTo( "Block 15" ) );
        assertThat(details.address().get().city().get().cityName().get(), equalTo( "Shanghai" ) );
        assertThat(details.address().get().city().get().country().get().countryName().get(), equalTo( "China" ) );
    }

    @Mixins(MyServiceMixin.class)
    public interface MyService extends ServiceComposite
    {
        PersonDetails details();
    }

    public abstract class MyServiceMixin
        implements MyService
    {
        @This
        Configuration<MyConfig> myconf;

        @Override
        public PersonDetails details()
        {
            return myconf.get().me().get();
        }
    }

    public interface MyConfig extends ConfigurationComposite
    {
        Property<PersonDetails> me();
    }

    public interface PersonDetails extends ValueComposite
    {
        Property<String> name();
        Property<Address> address();

    }

    public interface Address extends ValueComposite
    {
        Property<String> street1();
        Property<String> street2();
        Property<City> city();
    }

    public interface City extends ValueComposite
    {
        Property<String> cityName();
        Property<Country> country();
    }

    public interface Country extends ValueComposite
    {
        Property<String> countryName();
    }
}
