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

import org.qi4j.injection.scope.This;
import org.qi4j.injection.scope.Service;
import org.qi4j.service.Configuration;
import org.qi4j.library.uid.sequence.Sequencing;

public class NewThreadServiceMixin
    implements ThreadService
{
    private @This Configuration<ThreadServiceConfiguration> config;
    private int threadCount;
    @Service private Sequencing sequence;
    @Service private ThreadGroupService threadGroupService;

    public Thread newThread( Runnable runnable )
    {
        synchronized( this )
        {
            Integer max = config.configuration().maxThreads().get();
            if( threadCount >= max )
            {
                throw new MaximumThreadsException( max );
            }
            ThreadServiceConfiguration configuration = config.configuration();
            String name = configuration.threadBaseName().get() + sequence.newSequenceValue();
            String tgName = configuration.threadGroupName().get();
            ThreadGroup threadGroup = threadGroupService.getThreadGroup( tgName );
            return new Thread( threadGroup, new RunnableWrapper( runnable ), name );
        }
    }

    public ThreadServiceConfiguration configuration()
    {
        return config.configuration();
    }

    public class RunnableWrapper
        implements Runnable
    {
        private Runnable runnable;

        public RunnableWrapper( Runnable runnable )
        {
            this.runnable = runnable;
            threadCount++;
        }

        public void run()
        {
            runnable.run();
            threadCount--;
        }
    }
}
