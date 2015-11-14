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
package org.apache.zest.library.scheduler.schedule;

import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.EntityComposite;
import org.joda.time.DateTime;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;
import org.apache.zest.library.scheduler.Task;

/**
 * Represent the scheduling of a {@link Task}.
 */
public interface Schedule extends EntityComposite
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

    /** Returns true if the Schedule has been cancelled.
     *
     * @return true if the Schedule has been cancelled.
     */
    @UseDefaults
    Property<Boolean> cancelled();

    /** Returns true if the Schedule is currently running.
     *
     * @return true if the Schedule is currently running.
     */
    @UseDefaults
    Property<Boolean> running();

    /** Returns the number of times the {@link Task} has been executed.
     * <p>
     * Each time the {@link Task#run} method completes, with or without an {@link Exception}, this
     * counter is incremented by 1.
     * </p>
     *
     * @return true the number of Exception that has occurred when running the {@link Task}.
     */
    @UseDefaults
    Property<Long> executionCounter();

    /** Returns the number of Exception that has occurred when running the {@link Task}.
     * <p>
     * Each time the {@link Task#run} method throws a {@link RuntimeException}, this property
     * is incremenented by 1,
     * </p>
     *
     * @return true the number of Exception that has occurred when running the {@link Task}.
     */
    @UseDefaults
    Property<Long> exceptionCounter();

    /** Returns true if the Schedule is done and will not be executed any more times.
     *
     * @return true if the Schedule is done and will not be executed any more times.
     */
    @UseDefaults
    Property<Boolean> done();


    /**
     * Called just before the {@link org.apache.zest.library.scheduler.Task#run()} method is called.
     */
    void taskStarting();

    /**
     * Called directly after the {@link org.apache.zest.library.scheduler.Task#run()} method has been completed and
     * returned from the method normally.
     */
    void taskCompletedSuccessfully();

    /**
     * Called directly after the {@link org.apache.zest.library.scheduler.Task#run()} method has been completed but
     * threw a RuntimeException.
     * @param ex
     */
    void taskCompletedWithException( RuntimeException ex );

    /**
     * Compute the next time this schedule is to be run.
     *
     * @param from The starting time when to look for the next time it will run.
     *
     * @return The exact absolute time when this Schedule is to be run next time, or -1 if never
     */
    long nextRun( long from );

    /**
     * Return a representation of the Schedule in a human understandable format.
     *
     * @return A String representing this schedule.
     */
    String presentationString();

}
