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

public class QI247Test1
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TransientWithMixin.class );
    }

    private void checkToString( ObjectMethods withMixin )
    {
        assertThat( withMixin.toString(), equalTo( ObjectMethods.MESSAGE ) );
    }

    private void checkHashCode( ObjectMethods withMixin )
    {
        assertThat( withMixin.hashCode(), equalTo( ObjectMethods.CODE ) );
    }

    private void checkSelfEquals( ObjectMethods withMixin )
    {
        assertThat( withMixin, equalTo( withMixin ) );
    }

    private void checkTwoNotEqual( ObjectMethods first, ObjectMethods second )
    {
        assertThat( first.equals( second ), is( false ) );
    }

    // MIXIN

    @Test
    public void testWithMixinToString()
    {
        ObjectMethods withMixin = transientBuilderFactory.newTransient( ObjectMethods.class );
        checkToString( withMixin );
    }

    @Test
    public void testWithMixinHashCode()
    {
        ObjectMethods withMixin = transientBuilderFactory.newTransient( ObjectMethods.class );
        checkHashCode( withMixin );
    }

    @Test
    public void testWithMixinSelfEquals()
    {
        ObjectMethods withMixin = transientBuilderFactory.newTransient( ObjectMethods.class );
        checkSelfEquals( withMixin );
    }

    @Test
    public void testWithMixinSelfEquals2()
    {
        ObjectMethods withMixin = transientBuilderFactory.newTransient( ObjectMethods.class );
        assertThat( withMixin.equals( withMixin ), is( true ) );
    }

    @Test
    public void testWithMixinSelfSame()
    {
        ObjectMethods withMixin = transientBuilderFactory.newTransient( ObjectMethods.class );
        assertSame( withMixin, withMixin );
    }

    @Test
    public void testWithMixinTwoNotEqual()
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

    public static class ObjectMethodsMixin
        implements ObjectMethods
    {

        @Override
        public int hashCode()
        {
            return CODE;
        }

        @Override
        public String toString()
        {
            return MESSAGE;
        }

        public void someMethod()
        {
        }
    }

    @Mixins( ObjectMethodsMixin.class )
    public interface TransientWithMixin
        extends TransientComposite, ObjectMethods
    {
    }
}
