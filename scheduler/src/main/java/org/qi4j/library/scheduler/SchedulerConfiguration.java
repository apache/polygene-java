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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * @author Paul Merlin
 */
public interface SchedulerConfiguration
        extends ConfigurationComposite
{

    /**
     * @return Number of worker threads, defaults to the number of available cores.
     */
    @Optional
    Property<Integer> workersCount();

    /**
     * @return Size of the queue to use for holding tasks before they are run. Defaults to 10.
     */
    @Optional
    Property<Integer> workQueueSize();

    /**
     * @return SchedulerPulse rythm in seconds, default to 60.
     */
    @Optional
    Property<Integer> pulseRhythmSeconds();

    /**
     * @return SchedulerGarbageCollector rythm in seconds, default to 60.
     */
    @Optional
    Property<Integer> garbageCollectorRhythmSeconds();

    /**
     * @return If the scheduler must stop without waiting for running tasks.
     */
    @UseDefaults
    Property<Boolean> stopViolently();

}
