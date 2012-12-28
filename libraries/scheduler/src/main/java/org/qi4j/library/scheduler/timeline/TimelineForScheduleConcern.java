/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.scheduler.timeline;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.scheduler.schedule.Schedule;

public abstract class TimelineForScheduleConcern extends ConcernOf<Schedule>
    implements Schedule
{
    @This
    private TimelineScheduleState state;

    @Structure
    private Module module;

    @Override
    public void taskStarting()
    {
        addRecord( TimelineRecordStep.STARTED, "" );
        next.taskStarting();
    }

    @Override
    public void taskCompletedSuccessfully()
    {
        addRecord( TimelineRecordStep.SUCCESS, "" );
        next.taskCompletedSuccessfully();
    }

    @Override
    public void taskCompletedWithException( RuntimeException ex )
    {
        TimelineRecordStep step = TimelineRecordStep.FAILURE;
        String details = "Exception occurred:" + getStackTrace( ex );
        addRecord( step, details );
        next.taskCompletedWithException( ex );
    }

    private void addRecord( TimelineRecordStep step, String details )
    {
        ValueBuilder<TimelineRecord> builder = module.newValueBuilder( TimelineRecord.class );
        TimelineRecord prototype = builder.prototype();
        prototype.step().set( step );
        prototype.taskName().set( task().get().name().get() );
        List<String> tags = task().get().tags().get();
        prototype.taskTags().set( tags );
        prototype.timestamp().set( System.currentTimeMillis() );
        prototype.scheduleIdentity().set( this.identity().get() );
        prototype.details().set( details );
        TimelineRecord record = builder.newInstance();
        List<TimelineRecord> timelineRecords = state.history().get();
        timelineRecords.add( record );
        state.history().set( timelineRecords );
    }

    private String getStackTrace( RuntimeException ex )
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream( 1000 );
        BufferedOutputStream out = new BufferedOutputStream( baos );
        PrintStream print = new PrintStream( out );
        ex.printStackTrace( print );
        print.flush();
        return baos.toString();
    }
}
