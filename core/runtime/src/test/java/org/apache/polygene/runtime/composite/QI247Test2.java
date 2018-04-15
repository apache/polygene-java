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
package org.apache.polygene.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertSame;

public class QI247Test2
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TransientWithHandler.class );
    }

    private void checkToString( ObjectMethods instance )
    {
        assertThat( instance.toString(), equalTo( ObjectMethods.MESSAGE ) );
    }

    private void checkHashCode( ObjectMethods instance )
    {
        assertThat( instance.hashCode(), equalTo( ObjectMethods.CODE ) );
    }

    private void checkSelfEquals( ObjectMethods instance )
    {
        assertThat( instance, equalTo( instance ) );
    }

    private void checkTwoNotEqual( ObjectMethods first, ObjectMethods second )
    {
        assertThat( first.equals( second ), is( false ) );
    }

    //HANDLER

    @Test
    public void testWithHandlerToString()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient( ObjectMethods.class );
        checkToString( withHandler );
    }

    @Test
    public void testWithHandlerHashCode()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient( ObjectMethods.class );
        checkHashCode( withHandler );
    }

    @Test
    public void testWithHandlerSelfEquals()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient( ObjectMethods.class );
        checkSelfEquals( withHandler );
    }

    @Test
    public void testWithHandlerSelfEquals2()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient( ObjectMethods.class );
        assertThat( withHandler.equals( withHandler ), is( true ) );
    }

    @Test
    public void testWithHandlerSelfSame()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient( ObjectMethods.class );
        assertSame( withHandler, withHandler );
    }

    @Test
    public void testWithHandlerTwoNotEqual()
    {
        ObjectMethods first = transientBuilderFactory.newTransient( ObjectMethods.class );
        ObjectMethods second = transientBuilderFactory.newTransient( ObjectMethods.class );
        checkTwoNotEqual( first, second );
    }

    public interface ObjectMethods
    {
        String MESSAGE = "Does not work :(";

        int CODE = 123;

        void someMethod();
    }

    public static class ObjectMethodsHandler
        implements InvocationHandler
    {
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            System.out.println( "invoke(proxy, " + method.getName() + ", args" );
            if( "someMethod".equals( method.getName() ) )
            {
                System.out.println( "Hello." );
                return null;
            }
            else
            {
                throw new UnsupportedOperationException( method.toString() );
            }
        }

        public String toString()
        {
            return ObjectMethods.MESSAGE;
        }

        public int hashCode()
        {
            return ObjectMethods.CODE;
        }

        public boolean equals( Object o )
        {
            return o == this;
        }
    }

    @Mixins( ObjectMethodsHandler.class )
    public interface TransientWithHandler
        extends TransientComposite, ObjectMethods
    {
    }
}
