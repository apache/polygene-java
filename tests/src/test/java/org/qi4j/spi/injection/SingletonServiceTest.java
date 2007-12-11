/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.spi.injection;

import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.Service;
import org.qi4j.service.ServiceComposite;
import org.qi4j.spi.service.Singleton;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class SingletonServiceTest
    extends AbstractQi4jTest
{
    @Override public void configure( ModuleAssembly module )
    {
        module.addComposites( TestComposite1.class );
        module.addComposites( TestComposite2.class );
        module.addServiceProvider( new Singleton(), TestComposite2.class );
    }

    public void testServiceInjection()
        throws Exception
    {
        TestComposite1 test = compositeBuilderFactory.newCompositeBuilder( TestComposite1.class ).newInstance();
        assertEquals( "foo bar", test.foo() );
    }

    interface TestComposite1
        extends Test1, Composite
    {
    }

    @Mixins( Test1Mixin.class )
    interface Test1
    {
        String foo();
    }

    public static class Test1Mixin
        implements Test1
    {
        @Service TestComposite2 test2;

        public String foo()
        {
            return "foo " + test2.bar();
        }
    }

    interface TestComposite2
        extends Test2, ServiceComposite
    {
    }

    @Mixins( Test2Mixin.class )
    interface Test2
    {
        String bar();
    }

    public static class Test2Mixin
        implements Test2
    {
        public String bar()
        {
            return "bar";
        }
    }
}
