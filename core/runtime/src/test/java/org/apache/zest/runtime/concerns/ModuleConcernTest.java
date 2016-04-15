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

package org.apache.zest.runtime.concerns;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.concern.GenericConcern;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.AssemblyVisitorAdapter;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.TransientDeclaration;
import org.apache.zest.test.AbstractZestTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of declaring concern in assembly
 */
public class ModuleConcernTest
    extends AbstractZestTest
{
    public static boolean ok;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( FooComposite.class );

        module.layer().application().visit( new AssemblyVisitorAdapter<RuntimeException>()
        {
            @Override
            public void visitComposite( TransientDeclaration declaration )
            {
                declaration.withConcerns( TraceConcern.class );
            }
        }

        );
    }

    @Test
    public void testModuleConcerns()
    {
        transientBuilderFactory.newTransient( Foo.class ).test( "Foo", 42 );
        assertThat( "Concern has executed", ok, equalTo( true ) );
    }

    @Mixins( FooMixin.class )
    public interface FooComposite
        extends TransientComposite, Foo
    {
    }

    public interface Foo
    {
        String test( String foo, int bar );
    }

    public static class FooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + " " + bar;
        }
    }

    public static class TraceConcern
        extends GenericConcern
    {
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            ok = true;
            Object result = next.invoke( proxy, method, args );
            String str = method.getName() + Arrays.asList( args );
            System.out.println( str );
            return result;
        }
    }
}
