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

package org.qi4j.library.scheduler.schedule;

public final class ScheduleTime
    implements Comparable<ScheduleTime>
{
    public String scheduleIdentity;
    public long nextTime;

    public ScheduleTime( String scheduleIdentity, long nextTime )
    {
        if( scheduleIdentity == null )
        {
            throw new IllegalArgumentException( "null not allowed: " + scheduleIdentity );
        }
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
        if( !scheduleIdentity.equals( that.scheduleIdentity ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = scheduleIdentity.hashCode();
        result = 31 * result + (int) ( nextTime ^ ( nextTime >>> 32 ) );
        return result;
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
