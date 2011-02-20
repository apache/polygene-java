/*
 * Copyright (c) 2010, Lukasz Zielinski. All Rights Reserved.
 * Copyright (c) 2010, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.tests.jira.qi247;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.test.AbstractQi4jTest;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransientTest1 extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( TransientWithMixin.class );
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
        ObjectMethods withMixin = transientBuilderFactory.newTransient(ObjectMethods.class );
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
        ObjectMethods withMixin = transientBuilderFactory.newTransient(ObjectMethods.class );
        checkSelfEquals( withMixin );
    }

    @Test
    public void testWithMixinSelfEquals2()
    {
        ObjectMethods withMixin = transientBuilderFactory.newTransient(ObjectMethods.class );
        assertTrue( withMixin.equals( withMixin ) );
    }

    @Test
    public void testWithMixinSelfSame()
    {
        ObjectMethods withMixin = transientBuilderFactory.newTransient(ObjectMethods.class );
        assertSame( withMixin, withMixin );
    }

    @Test
    public void testWithMixinTwoNotEqual()
    {
        ObjectMethods first = transientBuilderFactory.newTransient(ObjectMethods.class );
        ObjectMethods second = transientBuilderFactory.newTransient(ObjectMethods.class );
        checkTwoNotEqual( first, second );
    }
}
