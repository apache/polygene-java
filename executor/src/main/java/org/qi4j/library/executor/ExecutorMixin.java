/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.qi4j.service.Activatable;

/**
 * Delegate Runnable's to a ScheduledThreadPoolExecutor
 */
public class ExecutorMixin
    implements Executor, Activatable
{
    private ExecutorService service;

    public void activate() throws Exception
    {
        service = new ScheduledThreadPoolExecutor( 10 );
    }

    public void passivate() throws Exception
    {
        System.out.println( "Shutdown executors" );
        service.shutdown();
    }

    public void execute( Runnable runnable )
    {
        service.execute( runnable );
    }
}
