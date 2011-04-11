/*
 * Copyright 1996-2005 Niclas Hedhman.
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
package org.qi4j.library.alarm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmHistory;

public class AlarmHistoryImpl
    implements java.io.Serializable, AlarmHistory
{
    private static final long serialVersionUID = 2L;

    private final ArrayList<AlarmEvent> eventList;
    private int maxSize = 30;
    private final HashMap<String, Integer> counters;

    public AlarmHistoryImpl()
    {
        eventList = new ArrayList<AlarmEvent>();
        counters = new HashMap<String, Integer>();
    }

    public AlarmEvent firstEvent()
    {
        synchronized( eventList )
        {
            if( eventList.size() == 0 )
            {
                return null;
            }
            return eventList.get( 0 );
        }
    }

    public AlarmEvent lastEvent()
    {
        synchronized( eventList )
        {
            if( eventList.size() == 0 )
            {
                return null;
            }
            return eventList.get( eventList.size() - 1 );
        }
    }

    public AlarmEvent eventAt( final int position )
    {
        synchronized( this )
        {
            if( eventList.size() <= position || position < 0 )
            {
                return null;
            }
            return eventList.get( position );
        }
    }

    public AlarmEvent eventAtEnd( final int position )
    {
        synchronized( eventList )
        {
            int size = eventList.size();
            if( size <= position || position < 0 )
            {
                return null;
            }
            return eventList.get( size - position - 1 );
        }
    }

    public void addEvent( final AlarmEvent event, final String trigger )
    {
        synchronized( this )
        {
            eventList.add( event );
            purge();
            Integer counter = counters.get( trigger );
            if( counter == null )
            {
                counter = 1;
            }
            else
            {
                counter = counter + 1;
            }
            counters.put( trigger, counter );
        }
    }

    synchronized public void setMaxSize( final int size )
    {
        maxSize = size;
        purge();
    }

    synchronized public int maxSize()
    {
        return maxSize;
    }

    public String toString()
    {
        synchronized( eventList )
        {
            StringBuffer buf = new StringBuffer();
            buf.append( "history[maxsize=" );
            buf.append( maxSize );
            buf.append( ", size=" );
            buf.append( eventList.size() );

            for( Map.Entry<String, Integer> entry : counters.entrySet() )
            {
                String type = entry.getKey();
                Integer counts = entry.getValue();
                buf.append( ", " );
                buf.append( type );
                buf.append( "=" );
                buf.append( counts );
            }
            buf.append( "]" );
            return buf.toString();
        }
    }

    synchronized protected void purge()
    {
        if( maxSize <= 0 )
        {
            eventList.clear();
        }

        while( eventList.size() > maxSize )
        {
            eventList.remove( 0 );
        }
    }

    public List<AlarmEvent> getAllAlarmEvents()
    {
        return eventList;
    }

    public Map<String, Integer> counters()
    {
        return counters;
    }

    public void resetAllCounters()
    {
        counters.clear();
    }

    public int activateCounter()
    {
        Integer counter = counters.get( Alarm.TRIGGER_ACTIVATION );
        if( counter == null )
        {
            return 0;
        }
        return counter;
    }

    public void resetActivateCounter()
    {
        counters.remove( Alarm.TRIGGER_ACTIVATION );
    }
}
