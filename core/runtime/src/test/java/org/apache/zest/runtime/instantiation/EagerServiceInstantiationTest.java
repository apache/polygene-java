/*
 * Copyright 2009 Niclas Hedhman.
 * Copyright 2012 Paul Merlin.
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

package org.apache.zest.runtime.instantiation;

import org.junit.Test;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.junit.Assert.assertEquals;

public class EagerServiceInstantiationTest
    extends AbstractZestTest
{
    private TestInfo testInfo;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        testInfo = new TestInfo();
        module.services( MyService.class ).setMetaInfo( testInfo ).instantiateOnStartup();
    }

    @Test
    public void givenServiceInstantiatedOnStartUpWhenTestIsRunExpectServiceToHaveRun()
    {
        assertEquals( "123", testInfo.test );
    }

    @Mixins( MyMixin.class )
    public interface MyService
        extends My, ServiceComposite
    {
    }

    public interface My
    {
        void doSomething();
    }

    public static class MyMixin
        implements My
    {
        public MyMixin( @Uses ServiceDescriptor descriptor )
        {
            descriptor.metaInfo( TestInfo.class ).test = "123";
        }

        public MyMixin()
        {
            System.out.println( "Constructor" );
        }

        public void doSomething()
        {
            System.out.println( "Execute" );
        }
    }

    public class TestInfo
    {
        private String test = "abc";
    }
}
