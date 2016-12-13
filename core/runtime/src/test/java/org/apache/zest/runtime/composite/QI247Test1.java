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
package org.apache.zest.runtime.composite;

import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertEquals( ObjectMethods.MESSAGE, withMixin.toString() );
    }

    private void checkHashCode( ObjectMethods withMixin )
    {
        assertEquals( ObjectMethods.CODE, withMixin.hashCode() );
    }

    private void checkSelfEquals( ObjectMethods withMixin )
    {
        assertEquals( withMixin, withMixin );
    }

    private void checkTwoNotEqual( ObjectMethods first, ObjectMethods second )
    {
        assertFalse( first.equals( second ) );
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
        assertTrue( withMixin.equals( withMixin ) );
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
