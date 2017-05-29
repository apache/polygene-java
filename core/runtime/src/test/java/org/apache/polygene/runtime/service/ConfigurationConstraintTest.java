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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.configuration.ConfigurationComposite;
import org.apache.polygene.api.constraint.Constraint;
import org.apache.polygene.api.constraint.ConstraintDeclaration;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.constraint.Constraints;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.entitystore.memory.MemoryEntityStoreService;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test of configuration for services that Constraints are respected.
 */
public class ConfigurationConstraintTest
{
    @Test
    public void givenConstrainedConfigurationWhenCorrectValueExpectNoFailure()
        throws Exception
    {
        SingletonAssembler underTest = new SingletonAssembler(
            module ->
            {
                module.defaultServices();
                module.services( MemoryEntityStoreService.class );
                module.services( TestService.class ).identifiedBy( "TestService1" );
                module.configurations( TestConfiguration.class );
            }
        );
        ServiceReference<TestService> service = underTest.module().findService( TestService.class );
        service.get().test();
    }

    @Test( expected = ConstraintViolationException.class )
    public void givenConstrainedConfigurationWhenIncorrectValueExpectConstraintViolationFailure()
        throws Exception
    {
        SingletonAssembler underTest = new SingletonAssembler(
            module ->
            {
                module.defaultServices();
                module.services( MemoryEntityStoreService.class );
                module.services( TestService.class ).identifiedBy( "TestService2" );
                module.configurations( TestConfiguration.class );
            }
        );
        ServiceReference<TestService> service = underTest.module().findService( TestService.class );
        service.get().test();
        fail( "Expected failure from constraint violation." );
    }

    @Mixins( TestMixin.class )
    public interface TestService
    {
        void test();
    }

    public interface TestConfiguration
        extends ConfigurationComposite
    {
        @Constrained
        Property<String> constrained();
    }

    public static class TestMixin
        implements TestService
    {
        @This
        Configuration<TestConfiguration> config;

        @Override
        public void test()
        {
            assertThat( config.get().constrained().get(), equalTo( "constrained" ) );
        }
    }

    @ConstraintDeclaration
    @Retention( RetentionPolicy.RUNTIME )
    @Constraints( ConstrainedConstraint.class )
    public @interface Constrained
    {
    }

    public static class ConstrainedConstraint
        implements Constraint<Constrained, String>
    {
        @Override
        public boolean isValid( Constrained annotation, String value )
        {
            return value.equals( "constrained" );
        }
    }
}