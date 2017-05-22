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
 */

package org.apache.polygene.test.performance.runtime.object;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.junit.Test;

/**
 * Tests performance of new object creation.
 */
public class ObjectCreationPerformanceTest
{

    @Test
    public void newInstanceForRegisteredObjectPerformance()
        throws ActivationException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
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
