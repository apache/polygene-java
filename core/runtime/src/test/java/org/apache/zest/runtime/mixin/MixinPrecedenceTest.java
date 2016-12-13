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

package org.apache.zest.runtime.mixin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test mixin declaration precedence satisfiedBy
 */
public class MixinPrecedenceTest
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite1.class,
                           TestComposite2.class,
                           TestComposite3.class );
    }

    @Test
    public void whenMultipleTypedMixinsPrecedence()
    {
        TestComposite1 instance = transientBuilderFactory.newTransient( TestComposite1.class );
        assertThat( "Mixin precedence", instance.AMethod(), equalTo( "A1" ) );
    }

    @Test
    public void whenGenericAndTypedMixinPrecedence()
    {
        TestComposite2 instance = transientBuilderFactory.newTransient( TestComposite2.class );
        assertThat( "Typed mixin is chosen over generic mixin", instance.AMethod(), equalTo( "A1" ) );
    }

    @Test
    public void whenMultipleGenericMixinsPrecedence()
    {
        TestComposite3 instance = transientBuilderFactory.newTransient( TestComposite3.class );
        assertEquals( "GM1", instance.AMethod() );
    }

    @Mixins( { AMixin1.class, AMixin2.class } )
    public static interface TestComposite1
        extends A, TransientComposite
    {
    }

    @Mixins( { GenericMixin1.class, AMixin1.class } )
    public static interface TestComposite2
        extends A, TransientComposite
    {
    }

    @Mixins( { GenericMixin1.class, GenericMixin2.class } )
    public static interface TestComposite3
        extends A, TransientComposite
    {
    }

    public interface A
    {
        String AMethod();
    }

    public static class AMixin1
        implements A
    {

        public String AMethod()
        {
            return "A1";
        }
    }

    public static class AMixin2
        implements A
    {

        public String AMethod()
        {
            return "A2";
        }
    }

    public static class GenericMixin1
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects )
            throws Throwable
        {
            return "GM1";
        }
    }

    public static class GenericMixin2
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects )
            throws Throwable
        {
            return "GM2";
        }
    }
}
