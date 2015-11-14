/*
 * Copyright (c) 2010-2012, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.library.scheduler.schedule;

import org.apache.zest.api.util.NullArgumentException;

public final class ScheduleTime
    implements Comparable<ScheduleTime>
{
    private String scheduleIdentity;
    private long nextTime;

    public ScheduleTime( String scheduleIdentity, long nextTime )
    {
        NullArgumentException.validateNotEmpty( "scheduleIdentity", scheduleIdentity );
        this.scheduleIdentity = scheduleIdentity;
        this.nextTime = nextTime;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        ScheduleTime that = (ScheduleTime) o;
        if( nextTime != that.nextTime )
        {
            return false;
        }
        return scheduleIdentity.equals( that.scheduleIdentity );
    }

    @Override
    public int hashCode()
    {
        int result = scheduleIdentity.hashCode();
        result = 31 * result + (int) ( nextTime ^ ( nextTime >>> 32 ) );
        return result;
    }

    public long nextTime()
    {
        return nextTime;
    }

    public String scheduleIdentity()
    {
        return scheduleIdentity;
    }

    @Override
    public int compareTo( ScheduleTime another )
    {
        if( this.nextTime < another.nextTime )
        {
            return -1;
        }
        else
        {
            if( this.nextTime == another.nextTime )
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }
}
