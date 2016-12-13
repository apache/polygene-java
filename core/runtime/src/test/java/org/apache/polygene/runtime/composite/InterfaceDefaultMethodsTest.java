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
 */
package org.apache.polygene.runtime.composite;

import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.constraints.annotation.NotEmpty;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Assert that interface default methods are mixed in composites.
 */
public class InterfaceDefaultMethodsTest extends AbstractPolygeneTest
{
    public interface DefaultMethods
    {
        default String sayHello( String name )
        {
            return "Hello, " + name + '!';
        }
    }

    public interface OverrideDefaultMethods extends DefaultMethods
    {
        @Override
        default String sayHello( String name )
        {
            return "Hello, overridden in " + name + '!';
        }
    }

    public static abstract class MixinDefaultMethods implements DefaultMethods
    {
        @Override
        public String sayHello( String name )
        {
            return "Hello, mixed in " + name + '!';
        }
    }

    public interface DefaultMethodsConstraints extends DefaultMethods
    {
        @Override
        default String sayHello( @NotEmpty String name )
        {
            return "Hello, " + name + '!';
        }
    }

    public interface DefaultMethodsConcerns extends DefaultMethods
    {
        // TODO Add concern
        @Override
        default String sayHello( String name )
        {
            return "Hello, " + name + '!';
        }
    }

    public interface DefaultMethodsSideEffects extends DefaultMethods
    {
        // TODO Add side effect
        @Override
        default String sayHello( String name )
        {
            return "Hello, " + name + '!';
        }
    }

    @Override
    public void assemble( final ModuleAssembly module ) throws AssemblyException
    {
        module.transients( DefaultMethods.class,
                           OverrideDefaultMethods.class,
                           MixinDefaultMethods.class,
                           DefaultMethodsConstraints.class,
                           DefaultMethodsConcerns.class,
                           DefaultMethodsSideEffects.class );
    }

    @Ignore( "ZEST-120" )
    @Test
    public void defaultMethods()
    {
        DefaultMethods composite = transientBuilderFactory.newTransient( DefaultMethods.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, John!" ) );
    }

    @Ignore( "ZEST-120" )
    @Test
    public void overrideDefaultMethods()
    {
        OverrideDefaultMethods composite = transientBuilderFactory.newTransient( OverrideDefaultMethods.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, overridden John!" ) );
    }

    @Ignore( "ZEST-120" )
    @Test
    public void mixinDefaultMethods()
    {
        MixinDefaultMethods composite = transientBuilderFactory.newTransient( MixinDefaultMethods.class );
        assertThat( composite.sayHello( "John" ), equalTo( "Hello, mixed in John!" ) );
    }

    @Ignore( "ZEST-120" )
    @Test
    public void defaultMethodsConstraints()
    {
        DefaultMethodsConstraints composite = transientBuilderFactory.newTransient( DefaultMethodsConstraints.class );
        try
        {
            composite.sayHello( "" );
        }
        catch( ConstraintViolationException ex )
        {
            assertThat( ex.getMessage(), containsString( "sayHello" ) );
        }
    }

    @Ignore( "ZEST-120" )
    @Test
    public void defaultMethodsConcerns()
    {
        fail( "Test not implemented" );
    }

    @Ignore( "ZEST-120" )
    @Test
    public void defaultMethodsSideEffects()
    {
        fail( "Test not implemented" );
    }
}
