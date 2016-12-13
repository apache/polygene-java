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

package org.apache.polygene.library.locking;

import org.junit.Test;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * JAVADOC
 */
public class LockingTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite.class );
    }


    @Test
    public void testLocking()
        throws InterruptedException
    {
        final TestComposite composite = transientBuilderFactory.newTransient( TestComposite.class );

        ExecutorService executor = Executors.newFixedThreadPool( 2 );

        executor.submit( new Runnable()
        {
            public void run()
            {
                System.out.println("Wait");
                try
                {
                    composite.readAndWait();
                    System.out.println("Wait done");
                }
                catch( Throwable e )
                {
                    e.printStackTrace();
                }
            }
        });

        executor.submit( new Runnable()
        {
            public void run()
            {
                System.out.println("Notify");
                try
                {
                    composite.readAndNotify();
                }
                catch( Throwable e )
                {
                    e.printStackTrace();
                }
                System.out.println("Notified");
            }
        });

        executor.shutdown();
        System.out.println("Finished: "+executor.awaitTermination( 5000, TimeUnit.MILLISECONDS ));
    }

    @Mixins( TestComposite.Mixin.class)
    public interface TestComposite
        extends TransientComposite, LockingAbstractComposite
    {
        void writeAndWait();

        void readAndWait()
            throws InterruptedException;

        void readAndNotify();

        abstract class Mixin
            implements TestComposite
        {
            @ReadLock
            public void readAndNotify()
            {
                synchronized( this )
                {
                    this.notifyAll();
                }
            }

            @ReadLock
            public void readAndWait()
                throws InterruptedException
            {
                synchronized( this )
                {
                    this.wait();
                }
            }

            @WriteLock
            public void writeAndWait()
            {
            }
        }
    }
}
