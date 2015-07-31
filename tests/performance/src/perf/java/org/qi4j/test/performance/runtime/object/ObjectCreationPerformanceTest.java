/*
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.test.performance.runtime.object;

import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

/**
 * Tests performance of new object creation.
 */
public class ObjectCreationPerformanceTest
{

    @Test
    public void newInstanceForRegisteredObjectPerformance()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( AnyObject.class );
            }
        };
        ObjectFactory objectFactory = assembler.module();
        for( int i = 0; i < 10; i++ )
        {
            testPerformance( objectFactory );
        }
    }

    private void testPerformance( ObjectFactory objectFactory )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            objectFactory.newObject( AnyObject.class );
        }

        long end = System.currentTimeMillis();
        System.out.println( end - start );
    }

    public static final class AnyObject
    {
    }

}
