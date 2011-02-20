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
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransientTest2 extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( TransientWithHandler.class );
    }

    private void checkToString( ObjectMethods instance )
    {
        assertEquals( ObjectMethods.MESSAGE, instance.toString() );
    }

    private void checkHashCode( ObjectMethods instance )
    {
        assertEquals( ObjectMethods.CODE, instance.hashCode() );
    }

    private void checkSelfEquals( ObjectMethods instance )
    {
        assertEquals( instance, instance );
    }

    private void checkTwoNotEqual( ObjectMethods first, ObjectMethods second )
    {
        assertFalse( first.equals( second ) );
    }

    //HANDLER

    @Test
    public void testWithHandlerToString()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient(ObjectMethods.class );
        checkToString( withHandler );
    }

    @Test
    public void testWithHandlerHashCode()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient(ObjectMethods.class );
        checkHashCode( withHandler );
    }

    @Test
    public void testWithHandlerSelfEquals()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient(ObjectMethods.class );
        checkSelfEquals( withHandler );
    }

    @Test
    public void testWithHandlerSelfEquals2()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient(ObjectMethods.class );
        assertTrue( withHandler.equals( withHandler ) );
    }

    @Test
    public void testWithHandlerSelfSame()
    {
        ObjectMethods withHandler = transientBuilderFactory.newTransient(ObjectMethods.class );
        assertSame( withHandler, withHandler );
    }

    @Test
    public void testWithHandlerTwoNotEqual()
    {
        ObjectMethods first = transientBuilderFactory.newTransient(ObjectMethods.class );
        ObjectMethods second = transientBuilderFactory.newTransient(ObjectMethods.class );
        checkTwoNotEqual( first, second );
    }
}
