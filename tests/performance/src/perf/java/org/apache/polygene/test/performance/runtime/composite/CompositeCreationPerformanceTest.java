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
package org.apache.polygene.test.performance.runtime.composite;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.junit.Test;

/**
 * Tests performance of new composite creation.
 */
public class CompositeCreationPerformanceTest
{
    @Test
    public void newInstanceForRegisteredCompositePerformance()
        throws ActivationException, InterruptedException
    {
        SingletonAssembler assembler = new SingletonAssembler(
            module -> {
                module.transients( AnyComposite.class );
                module.objects( AnyObject.class );
                module.values( AnyValue.class );
            }
        );
        int warmups = 10;
        int runs = 20;
        long waitBeforeRun = 1000;
        long waitBetweenRuns = 500;
        long timeForJavaObject = 0;
        {
            // Warmup
            for( int i = 0; i < warmups; i++ )
            {
                testJavaObjectCreationPerformance( false );
            }
            Thread.sleep( waitBeforeRun );
            // Run
            for( int i = 0; i < runs; i++ )
            {
                timeForJavaObject += testJavaObjectCreationPerformance( true );
                Thread.sleep( waitBetweenRuns );
            }
            timeForJavaObject = timeForJavaObject / runs;
        }
        long timeForTransientComposite = 0;
        {
            TransientBuilderFactory module = assembler.module();
            // Warmup
            for( int i = 0; i < warmups; i++ )
            {
                testCompositeCreationPerformance( module, false );
            }
            Thread.sleep( waitBeforeRun );
            // Run
            for( int i = 0; i < runs; i++ )
            {
                timeForTransientComposite += testCompositeCreationPerformance( module, true );
                Thread.sleep( waitBetweenRuns );
            }
            timeForTransientComposite = timeForTransientComposite / runs;
        }
        long timeForManagedObject = 0;
        {
            ObjectFactory objectFactory = assembler.module();
            // Warmup
            for( int i = 0; i < warmups; i++ )
            {
                testObjectCreationPerformance( objectFactory, false );
            }
            Thread.sleep( waitBeforeRun );
            // Run
            for( int i = 0; i < runs; i++ )
            {
                timeForManagedObject += testObjectCreationPerformance( objectFactory, true );
                Thread.sleep( waitBetweenRuns );
            }
            timeForManagedObject = timeForManagedObject / runs;
        }
        long timeForValueComposite = 0;
        {
            ValueBuilderFactory valueBuilderFactory = assembler.module();
            // Warmup
            for( int i = 0; i < warmups; i++ )
            {
                testValueCreationPerformance( valueBuilderFactory, false );
            }
            Thread.sleep( waitBeforeRun );
            // Run
            for( int i = 0; i < runs; i++ )
            {
                timeForValueComposite += testValueCreationPerformance( valueBuilderFactory, true );
                Thread.sleep( waitBetweenRuns );
            }
            timeForValueComposite = timeForValueComposite / runs;
        }

        long timeForTransientCompositeBuilder = 0;
        {
            TransientBuilderFactory module = assembler.module();
            // Warmup
            for( int i = 0; i < warmups; i++ )
            {
                testCompositeCreationWithBuilderPerformance( module, false );
            }
            Thread.sleep( waitBeforeRun );
            // Run
            for( int i = 0; i < runs; i++ )
            {
                timeForTransientCompositeBuilder += testCompositeCreationWithBuilderPerformance( module, true );
                Thread.sleep( waitBetweenRuns );
            }
            timeForTransientCompositeBuilder = timeForTransientCompositeBuilder / runs;
        }
        long timeForValueCompositeBuilder = 0;
        {
            ValueBuilderFactory valueBuilderFactory = assembler.module();
            // Warmup
            for( int i = 0; i < warmups; i++ )
            {
                testValueCreationWithBuilderPerformance( valueBuilderFactory, false );
            }
            Thread.sleep( waitBeforeRun );
            // Run
            for( int i = 0; i < runs; i++ )
            {
                timeForValueCompositeBuilder += testValueCreationWithBuilderPerformance( valueBuilderFactory, true );
                Thread.sleep( waitBetweenRuns );
            }
            timeForValueCompositeBuilder = timeForValueCompositeBuilder / runs;
        }

        System.out.println( "----" );
        System.out.println( "Transient: " + ( timeForTransientComposite / timeForJavaObject ) + "x" );
        System.out.println( "TransientBuilder: " + ( timeForTransientCompositeBuilder / timeForJavaObject ) + "x" );
        System.out.println( "Value: " + ( timeForValueComposite / timeForJavaObject ) + "x" );
        System.out.println( "ValueBuilder: " + ( timeForValueCompositeBuilder / timeForJavaObject ) + "x" );
        System.out.println( "Object: " + ( timeForManagedObject / timeForJavaObject ) + "x" );
    }

    private long testCompositeCreationPerformance( TransientBuilderFactory module, boolean run )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            module.newTransient( AnyComposite.class );
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        if( run )
        {
            System.out.println( "Composite Creation Time:" + time + " nanoseconds per composite" );
        }
        return time;
    }

    private long testCompositeCreationWithBuilderPerformance( TransientBuilderFactory module, boolean run )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            module.newTransientBuilder( AnyComposite.class ).newInstance();
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        if( run )
        {
            System.out.println( "Composite (builder) Creation Time:" + time + " nanoseconds per composite" );
        }
        return time;
    }

    private long testValueCreationPerformance( ValueBuilderFactory valueBuilderFactory, boolean run )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            valueBuilderFactory.newValue( AnyValue.class );
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        if( run )
        {
            System.out.println( "Value Creation Time:" + time + " nanoseconds per composite" );
        }
        return time;
    }

    private long testValueCreationWithBuilderPerformance( ValueBuilderFactory valueBuilderFactory, boolean run )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            valueBuilderFactory.newValueBuilder( AnyValue.class ).newInstance();
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        if( run )
        {
            System.out.println( "Value (builder) Creation Time:" + time + " nanoseconds per composite" );
        }
        return time;
    }

    private long testObjectCreationPerformance( ObjectFactory objectFactory, boolean run )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            objectFactory.newObject( AnyObject.class );
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        if( run )
        {
            System.out.println( "Polygene Object Creation Time:" + time + " nanoseconds per object" );
        }
        return time;
    }

    private long testJavaObjectCreationPerformance( boolean run )
    {
        long start = System.currentTimeMillis();
        int iter = 1000000;
        for( int i = 0; i < iter; i++ )
        {
            new AnyObject();
        }

        long end = System.currentTimeMillis();
        long time = 1000000L * ( end - start ) / iter;
        if( run )
        {
            System.out.println( "Java Object Creation Time:" + time + " nanoseconds per object" );
        }
        return time;
    }

    public interface AnyComposite
        extends TransientComposite
    {
    }

    public interface AnyValue
        extends ValueComposite
    {
    }

    public static class AnyObject
    {
    }
}
