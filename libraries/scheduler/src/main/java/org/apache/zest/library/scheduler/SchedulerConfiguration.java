/*
 * Copyright (c) 2010-2012, Paul Merlin.
 * Copyright (c) 2012, Niclas Hedhman.
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
package org.apache.zest.library.scheduler;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.property.Property;

/**
 * Configuration for the {@link Scheduler}.
 *
 * Every property has a default value, you can use a {@link Scheduler} without providing any.
 */
public interface SchedulerConfiguration
{
// START SNIPPET: configuration
    /**
     * @return Number of worker threads, optional and defaults to the number of available cores.
     */
    @Optional @UseDefaults
    Property<Integer> workersCount();

    /**
     * @return Size of the queue to use for holding tasks before they are run, optional and defaults to 10.
     */
    @Optional @UseDefaults
    Property<Integer> workQueueSize();

// END SNIPPET: configuration
}
