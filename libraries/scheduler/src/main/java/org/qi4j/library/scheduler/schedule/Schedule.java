/*
 * Copyright (c) 2010-2012, Paul Merlin. All Rights Reserved.
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.scheduler.schedule;

import org.joda.time.DateTime;
import org.qi4j.api.association.Association;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.library.scheduler.Task;

/**
 * Represent the scheduling of a {@link Task}.
 */
public interface Schedule extends Identity
{
    /**
     * @return The Association to the Task to be executed when it is time.
     */
    Association<Task> task();

    /** The first run of this Schedule.
     *
     * @return The property containing the first time this Schedule will be run.
     */
    @Immutable
    Property<DateTime> start();

    /**
     * Called just before the {@link org.qi4j.library.scheduler.Task#run()} method is called.
     */
    void taskStarting();

    /**
     * Called directly after the {@link org.qi4j.library.scheduler.Task#run()} method has been completed and
     * returned from the method normally.
     */
    void taskCompletedSuccessfully();

    /**
     * Called directly after the {@link org.qi4j.library.scheduler.Task#run()} method has been completed but
     * threw a RuntimeException.
     * @param ex
     */
    void taskCompletedWithException( RuntimeException ex );

    /**
     * @return True if the associated {@link org.qi4j.library.scheduler.Task} is currently running, false otherwise
     */
    boolean isTaskRunning();

    /**
     * Compute the next time this schedule is to be run.
     *
     * @param from The starting time when to look for the next time it will run.
     *
     * @return The exact absolute time when this Schedule is to be run next time.
     */
    long nextRun( long from );

    /**
     * Return a representation of the Schedule in a human understandable format.
     *
     * @return A String representing this schedule.
     */
    String presentationString();
}
