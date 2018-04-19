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
package org.apache.polygene.runtime.appliesto;

import java.lang.reflect.Method;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.common.AppliesToFilter;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.concern.GenericConcern;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test of the AppliesToFilter
 */
public class AppliesToFilterTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( SomeComposite.class );
    }

    @Test
    public void givenAnAppliesToFilterWhenAppliedThenFilterMethods()
        throws Exception
    {
        Some some = transientBuilderFactory.newTransient( Some.class );
        assertThat( some.doStuff1(), equalTo( "," ) );
        assertThat( some.doStuff2(), equalTo( ",,.." ) );
        assertThat( some.doStuff3(), equalTo( ",,," ) );
    }

    @Concerns( MyConcern.class )
    @Mixins( SomeMixin.class )
    private interface SomeComposite
        extends Some, TransientComposite
    {
    }

    public static interface Some
    {
        String doStuff1();

        String doStuff2();

        String doStuff3();
    }

    @AppliesTo( TestFilter.class )
    public static class MyConcern
        extends GenericConcern
    {
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            String str = (String) next.invoke( proxy, method, args );
            return str + "..";
        }
    }

    public static class SomeMixin
        implements Some
    {

        public String doStuff1()
        {
            return ",";
        }

        public String doStuff2()
        {
            return ",,";
        }

        public String doStuff3()
        {
            return ",,,";
        }
    }

    public static class TestFilter
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            return method.getName().equals( "doStuff2" );
        }
    }
}