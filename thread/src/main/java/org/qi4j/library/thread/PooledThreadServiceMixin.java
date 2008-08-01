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

import java.util.LinkedList;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.This;
import org.qi4j.library.uid.sequence.Sequencing;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;

public class PooledThreadServiceMixin
    implements ThreadService, Activatable
{
    @This private Configuration<ThreadServiceConfiguration> config;
    @Service private Sequencing sequence;
    @Service private ThreadGroupService threadGroupService;
    private LinkedList<RunnableThread> pool;
    private int threadCount;

    public PooledThreadServiceMixin()
    {
        pool = new LinkedList<RunnableThread>();
        threadCount = 0;
    }

    public Thread newThread( Runnable runnable )
    {
        synchronized( this )
        {
            if( pool.isEmpty() )
            {
                Integer max = config.configuration().maxThreads().get();
                if( threadCount >= max )
                {
                    throw new MaximumThreadsException( max );
                }
                createNewThread();
            }
            RunnableThread rt = pool.removeFirst();
            rt.runnable.currentRunnable( runnable );
            return rt.thread;
        }
    }

    public ThreadServiceConfiguration configuration()
    {
        return config.configuration();
    }

    public void activate()
        throws Exception
    {
        pool = new LinkedList<RunnableThread>();
        int prefered = config.configuration().preferedNumberOfThreads().get();
        for( int i = 0; i < prefered; i++ )
        {
            createNewThread();
        }
    }

    private void createNewThread()
    {
        ThreadServiceConfiguration configuration = config.configuration();
        String tgName = configuration.threadGroupName().get();
        ThreadGroup group = threadGroupService.getThreadGroup( tgName );
        String name = configuration.threadBaseName().get() + "-" + sequence.newSequenceValue();
        PooledRunnableWrapper runnable = new PooledRunnableWrapper();
        Thread t = new Thread( group, runnable, name );
        RunnableThread runnableThread = new RunnableThread( t, runnable );
        runnable.poolInstance = runnableThread;
        threadCount++;
        pool.add( runnableThread );
    }

    public void passivate()
        throws Exception
    {
        for( RunnableThread thread : pool )
        {
            threadCount = 0;
            thread.runnable.run = false;
            thread.thread.interrupt();
        }
    }

    public static class RunnableThread
    {
        private final Thread thread;
        private final PooledRunnableWrapper runnable;

        public RunnableThread( Thread thread, PooledRunnableWrapper runnable )
        {
            this.thread = thread;
            this.runnable = runnable;
        }
    }

    public class PooledRunnableWrapper
        implements Runnable
    {
        private boolean run;
        private Runnable current;
        private RunnableThread poolInstance;

        public void currentRunnable( Runnable current )
        {
            this.current = current;
            synchronized( this )
            {
                notifyAll();
            }
        }

        public void run()
        {
            run = true;
            while( run )
            {
                try
                {
                    synchronized( this )
                    {
                        while( current == null )
                        {
                            wait( 1000 );
                        }
                    }
                    current.run();
                    pool.addLast( poolInstance );
                }
                catch( InterruptedException e )
                {
                    run = false;
                }
            }
        }
    }
}
