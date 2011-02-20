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
package org.qi4j.test.object;

import org.junit.Test;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class ObjectCreationPerformanceTest
{

    /**
     * Tests performance of new object creation
     */
    @Test
    public void newInstanceForRegisteredObjectPerformance()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addObjects( AnyObject.class );
            }
        };
        ObjectBuilderFactory objectBuilderFactory = assembler.objectBuilderFactory();
        for( int i = 0; i < 10; i++ )
        {
            testPerformance( objectBuilderFactory );
        }
    }

    private void testPerformance( ObjectBuilderFactory objectBuilderFactory )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            objectBuilderFactory.newObject( AnyObject.class );
        }

        long end = System.currentTimeMillis();
        System.out.println( end - start );
    }

    public static final class AnyObject
    {
    }
}
