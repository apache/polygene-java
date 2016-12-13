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

import org.junit.Test;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.configuration.ConfigurationComposite;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of configuration for services
 */
public class ConfigurationTest
    extends AbstractPolygeneTest
{
    @Service
    ServiceReference<HelloWorldService> service;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( this.getClass() );
        module.entities( HelloWorldConfiguration.class );
        module.services( HelloWorldService.class ).identifiedBy( "HelloWorldService" );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void whenConfiguredThenSayHelloWorks()
        throws Exception
    {
        UnitOfWork unit = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<HelloWorldConfiguration> entityBuilder = unit.newEntityBuilder( HelloWorldConfiguration.class, service.identity() );
        HelloWorldConfiguration config = entityBuilder.instance();
        config.phrase().set( "Hey" );
        config.name().set( "Universe" );
        entityBuilder.newInstance();
        unit.complete();

        assertThat( "result is correct", service.get().sayHello(), equalTo( "Hey Universe" ) );
    }

    @Test
    public void whenUnconfiguredThenSayHelloGivesDefaults()
        throws Exception
    {
        assertThat( "result is correct", service.get().sayHello(), equalTo( "Hello World" ) );
    }

    @Test
    public void givenConfiguredServiceWhenReconfiguredAndRefreshedThenNewConfigurationIsUsed()
        throws Exception
    {
        HelloWorldConfiguration config;

        {
            UnitOfWork unit = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<HelloWorldConfiguration> entityBuilder = unit.newEntityBuilder( HelloWorldConfiguration.class, service.identity() );
            config = entityBuilder.instance();
            config.phrase().set( "Hello" );
            config.name().set( "World" );
            config = entityBuilder.newInstance();
            unit.complete();
        }

        assertThat( "result is correct", service.get().sayHello(), equalTo( "Hello World" ) );

        {
            UnitOfWork unit = unitOfWorkFactory.newUnitOfWork();
            config = unit.get( config );
            config.phrase().set( "Hey" );
            config.name().set( "Universe" );
            unit.complete();
        }

        assertThat( "new configuration is not used", service.get().sayHello(), equalTo( "Hello World" ) );

        service.get().refresh();

        assertThat( "new configuration is used", service.get().sayHello(), equalTo( "Hey Universe" ) );
    }

    public interface HelloWorld
    {
        String sayHello();
    }

    @Mixins( HelloWorldMixin.class )
    public interface HelloWorldService
        extends HelloWorld, ServiceComposite, Configuration
    {
    }

    public interface HelloWorldConfiguration
        extends ConfigurationComposite
    {
        @UseDefaults
        Property<String> phrase();

        @UseDefaults
        Property<String> name();
    }

    public static class HelloWorldMixin
        implements HelloWorld
    {
        @This
        Configuration<HelloWorldConfiguration> config;

        public String sayHello()
        {
            return config.get().phrase() + " " + config.get().name();
        }
    }
}