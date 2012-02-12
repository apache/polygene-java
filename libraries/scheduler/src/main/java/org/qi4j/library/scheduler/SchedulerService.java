/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.scheduler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.scheduler.slaves.SchedulerThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( {
             SchedulerActivation.class,
             SchedulerMixin.class,
             SchedulerThreadFactory.class,
             SchedulerService.ScheduleRejectedHandler.class
         } )
public interface SchedulerService extends Scheduler, Activatable, Configuration, ServiceComposite
{
    public class ScheduleRejectedHandler
        implements RejectedExecutionHandler
    {
        public static final Logger logger = LoggerFactory.getLogger( SchedulerService.class );

        @Override
        public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
        {
            logger.error( "Runnable [" + r + "] was rejected by executor [" + executor + "]" );
        }
    }
}
