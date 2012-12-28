/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.appliesto;

import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test of the AppliesToFilter
 */
public class AppliesToFilterTest
    extends AbstractQi4jTest
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
        Some some = module.newTransient( Some.class );
        Assert.assertEquals( ",", some.doStuff1() );
        Assert.assertEquals( ",,..", some.doStuff2() );
        Assert.assertEquals( ",,,", some.doStuff3() );
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