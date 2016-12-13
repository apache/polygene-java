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

package org.apache.zest.runtime.injection;

import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

/**
 * Test creation of object graph, where @Uses is used to refer to instances
 */
public class UsesGraphTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( A.class, B.class, C.class, D.class );
    }

    @Test
    public void givenGraphDependenciesWhenInstantiateAThenGetSameReferences()
    {
        D d = new D();
        A a = objectFactory.newObject( A.class, objectFactory.newObject( C.class, d ), d );

        Assert.assertThat( "Same reference expected", a.c, equalTo( a.b.c ) );
        Assert.assertThat( "Same reference expected", a.d, equalTo( a.b.c.d ) );
    }

    @Test
    public void givenGraphDependenciesWhenInstantiateUsingBuildersThenDontGetSameReferences()
    {
        A a = objectFactory.newObject( A.class );
        Assert.assertThat( "Same reference not expected", a.c, not( equalTo( a.b.c ) ) );
        Assert.assertThat( "Same reference not expected", a.d, not( equalTo( a.b.c.d ) ) );
    }

    static public class A
    {
        @Uses
        B b;

        @Uses
        C c;

        @Uses
        D d;
    }

    static public class B
    {
        @Uses
        C c;
    }

    static public class C
    {
        @Uses
        D d;
    }

    static public class D
    {
    }
}
