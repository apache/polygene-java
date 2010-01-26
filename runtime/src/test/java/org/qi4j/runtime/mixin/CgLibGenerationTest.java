/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.runtime.mixin;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.*;

public class CgLibGenerationTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( SomeTransient.class );
    }

    @Test
    public void givenTwoInstancesOfSameTypeWhenMakingMethodCallThenEnsureThisRoutingIsCorrect()
    {
        Some some1 = transientBuilderFactory.newTransient( Some.class );
        Some some2 = transientBuilderFactory.newTransient( Some.class );

        assertEquals( "Result: Count:0", some1.result() );
        assertEquals( "doSome: Result: Count:0", some1.doSome() );
        assertEquals( "Result: Count:1", some2.result() );
        assertEquals( "doSome: Result: Count:1", some2.doSome() );
    }

    @Concerns( SomeConcern.class )
    @Mixins( SomeMixin.class )
    public interface SomeTransient
        extends Some, TransientComposite
    {
    }

    public interface Some
    {
        String doSome();

        String result();
    }

    public static class SomeConcern
        extends ConcernOf<Some>
        implements Some
    {

        public String doSome()
        {
            String result = next.doSome();

            return "doSome: " + result;
        }

        public String result()
        {
            return "Result: " + next.result();
        }
    }

    public static class SomeMixin
        implements Some
    {
        private static int instance = 0;

        private int counter;

        public SomeMixin()
        {
            counter = instance++;
        }

        public String doSome()
        {
            return result();
        }

        public String result()
        {
            return "Count:" + counter;
        }
    }
}
