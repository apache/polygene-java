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

package org.apache.zest.api.object;

import org.junit.Test;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.junit.Assert.assertNotNull;

/**
 * JAVADOC
 */
public class ObjectBuilderTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( A.class, B.class, C.class, D.class );
    }

    @Test
    public void testNotProvidedUses()
    {
        A a = objectFactory.newObject( A.class );
        assertNotNull( a );
        assertNotNull( a.b );
        assertNotNull( a.b.c );
        assertNotNull( a.b.c.d );
    }

    public static class A
    {
        @Uses
        B b;
    }

    public static class B
    {
        @Uses
        C c;
    }

    public static class C
    {
        @Uses
        D d;
    }

    public static class D
    {

    }
}
