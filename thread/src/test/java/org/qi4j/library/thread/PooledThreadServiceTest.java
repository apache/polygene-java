/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.library.thread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.Assert;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.library.thread.assembly.PooledThreadServiceAssembler;
import org.qi4j.test.AbstractQi4jTest;
import java.util.ArrayList;

public class PooledThreadServiceTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( UnderTestComposite.class );
        module.addServices( MemoryEntityStoreService.class );
        module.addAssembler( new PooledThreadServiceAssembler() );
    }

    @Test
    public void whenUsingPooledThreadProviderThenSameThreadsAreHandedBack()
        throws Exception
    {
        UnderTest underTest = compositeBuilderFactory.newComposite( UnderTest.class );
        ArrayList<Thread> threads = new ArrayList<Thread>();
        int poolsize = underTest.maxThreads();
        TestRunnable r1 = new TestRunnable();
        threads.add( underTest.fetchThread( r1 ) );
        for( int i=1 ; i < poolsize; i++ )
        {
            TestRunnable r2 = new TestRunnable();
            threads.add( underTest.fetchThread( r2 ) );
        }
        try
        {
            TestRunnable r2 = new TestRunnable();
            underTest.fetchThread( r2 );
            Assert.fail( "Should have thrown a MaxixmumThreadsException.");
        } catch( MaximumThreadsException e )
        { // ignore
        }
        for( int i=0; i < poolsize; i++ )
        {
            Thread t1 = threads.get(i);

            for( int j=0; j < poolsize; j++ )
            {
                Thread t2 = threads.get(j);
                assertFalse( (i != j ) && t1.equals( t2 ) );
            }
            t1.start();
        }
        Thread.sleep( 100 );
        Thread t1 = threads.get(0);
        r1.stop();
        Thread.sleep( 100 );
        TestRunnable r3 = new TestRunnable();
        Thread t3 = underTest.fetchThread( r3 );
        assertEquals( t1, t3 );
    }

    public interface UnderTest
    {
        Thread fetchThread( Runnable runnable );
        int maxThreads();
    }

    @Mixins( UnderTestMixin.class )
    public interface UnderTestComposite extends UnderTest, Composite
    {
    }

    public static class UnderTestMixin
        implements UnderTest
    {
        @Service private ThreadService service;

        public Thread fetchThread( Runnable runnable )
        {
            return service.newThread( runnable );
        }

        public int maxThreads()
        {
            return service.configuration().maxThreads().get();
        }
    }

    public static class TestRunnable
        implements Runnable
    {
        private Thread thread;
        private boolean run;

        public Thread getThread()
        {
            return thread;
        }

        public void stop()
        {
            run = false;
        }

        public void run()
        {
            run = true;
            thread = Thread.currentThread();
            int count = 0;
            while( run )
            {
                try
                {
                    Thread.sleep( 10 );
                }
                catch( InterruptedException e )
                {
                }
                count++;
            }
        }
    }
}