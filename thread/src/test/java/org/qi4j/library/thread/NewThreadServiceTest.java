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

import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.library.thread.assembly.NewThreadServiceAssembler;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.junit.Test;
import static org.junit.Assert.assertFalse;

public class NewThreadServiceTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( UnderTestComposite.class );
        module.addServices( MemoryEntityStoreService.class );
        module.addAssembler( new NewThreadServiceAssembler() );
    }

    @Test
    public void whenUsingNewThreadProviderThenNewThreadsAreHandedBack()
        throws Exception
    {
        TestRunnable r1 = new TestRunnable();
        TestRunnable r2 = new TestRunnable();
        UnderTest underTest = compositeBuilderFactory.newComposite( UnderTest.class );
        Thread t1 = underTest.fetchThread( r1 );
        Thread t2 = underTest.fetchThread( r2 );
        assertFalse( t1.equals(t2) );
        t1.start();
        t2.start();
        Thread.sleep( 20 );
        // Clean up
        r1.run = false;
        r2.run = false;
        t1.interrupt();
        t2.interrupt();
    }

    public interface UnderTest
    {
        Thread fetchThread( Runnable runnable );
    }

    @Mixins( UnderTestMixin.class)
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
            while( run )
            {
                try
                {
                    Thread.sleep(10);
                }
                catch( InterruptedException e )
                {
                }
            }
        }
    }
}
