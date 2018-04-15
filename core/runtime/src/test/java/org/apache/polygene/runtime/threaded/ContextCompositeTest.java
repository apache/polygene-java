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
package org.apache.polygene.runtime.threaded;

import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.composite.CompositeContext;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ContextCompositeTest
    extends AbstractPolygeneTest
{

    @Test
    public void testThreadScope()
        throws InterruptedException
    {
        for( int i = 0; i < 5; i++ )
        {
            TransientBuilder<MyCompositeContext> builder = transientBuilderFactory.newTransientBuilder( MyCompositeContext.class );
            builder.prototypeFor( MyData.class ).data().set( 0 );
            MyCompositeContext context = new CompositeContext<>( module, MyCompositeContext.class ).proxy();

            Worker w1;
            Worker w2;
            MyCompositeContext c1 = builder.newInstance();
            {
                w1 = new Worker( "w1", context, 100, 0 );
                w2 = new Worker( "w2", context, 400, 20 );
                w2.start();
                w1.start();
            }
            w1.join();
            w2.join();
            System.out.println( "W1: " + w1.getData() );
            System.out.println( "W2: " + w2.getData() );
            assertThat( (int) c1.data().get(), equalTo( 0 ) );
            assertThat( w1.getData(), equalTo( 100 ) );
            assertThat( w2.getData(), equalTo( 400 ) );
        }
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( MyCompositeContext.class );
    }

    public static interface MyCompositeContext
        extends TransientComposite, MyData
    {
    }

    public static interface MyData
    {
        @UseDefaults
        Property<Integer> data();
    }

    static class Worker
        extends Thread
    {
        private MyCompositeContext composite;
        private int loops;
        private final String spaces;
        private int data;

        public Worker( String name, MyCompositeContext composite, int loops, int spaces )
        {
            super( name );
            this.composite = composite;
            this.loops = loops;
            StringBuilder builder = new StringBuilder();
            for( int i = 0; i < spaces; i++ )
            {
                builder.append( " " );
            }
            this.spaces = builder.toString();
        }

        public void run()
        {
            int counter = 0;
            int mismatchCounter = 0;
            Property<Integer> readProperty = composite.data();
            Property<Integer> writeProperty = composite.data();
            try
            {
                int oldValue = 0;
                for( int i = 0; i < loops; i++ )
                {
                    int value;
                    value = readProperty.get();
                    if( oldValue != value )
                    {
                        mismatchCounter++;
                    }
                    value = value + 1;
                    oldValue = value;
                    Thread.sleep( Math.round( Math.random() * 3 ) );
                    writeProperty.set( value );
                    counter++;
                }
            }
            catch( InterruptedException e )
            {
                e.printStackTrace();
            }
            data = composite.data().get();
            System.out
                .println( counter + "/" + loops + "    " + data + ", " + mismatchCounter + ", " + System.identityHashCode( readProperty ) + ", " + System
                    .identityHashCode( writeProperty ) );
        }

        public int getData()
        {
            return data;
        }
    }
}
