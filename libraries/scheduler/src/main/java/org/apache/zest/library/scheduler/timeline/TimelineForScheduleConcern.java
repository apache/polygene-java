/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.library.scheduler.timeline;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.library.scheduler.Schedule;

public abstract class TimelineForScheduleConcern
    extends ConcernOf<Schedule>
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
    public void taskCompletedWithException( Throwable ex )
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

    private String getStackTrace( Throwable ex )
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream( 1000 );
        BufferedOutputStream out = new BufferedOutputStream( baos );
        PrintStream print = new PrintStream( out );
        ex.printStackTrace( print );
        print.flush();
        return baos.toString();
    }
}
