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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmHistory;
import org.qi4j.library.alarm.Alarm;

public class AlarmHistoryImpl
    implements java.io.Serializable, AlarmHistory
{
    private static final long serialVersionUID = 2L;

    private final ArrayList eventList;
    private int maxSize = 30;
    private HashMap counters;

    public AlarmHistoryImpl()
    {
        eventList = new ArrayList();
        counters = new HashMap();
    }

    public AlarmEvent getFirst()
    {
        synchronized( eventList )
        {
            if( eventList.size() == 0 )
                return null;
            return (AlarmEvent) eventList.get(0);
        }
    }

    public AlarmEvent getLast()
    {
        synchronized( eventList )
        {
            if( eventList.size() == 0 )
                return null;
            return (AlarmEvent) eventList.get( eventList.size()-1);
        }
    }

    public AlarmEvent getAt( final int position )
    {
        synchronized( this )
        {
            if( eventList.size() <= position || position < 0 )
                return null;
            return (AlarmEvent) eventList.get(position);
        }
    }

    public AlarmEvent getAtFromLast( final int position )
    {
        synchronized( eventList )
        {
            int size = eventList.size();
            if( size <= position || position < 0 )
                return null;
            return (AlarmEvent) eventList.get( size-position-1 );
        }
    }

    public void addEvent( final AlarmEvent event, final String trigger )
    {
        synchronized( this )
        {
            eventList.add(event);
            purge();
            Integer counter = (Integer) counters.get( trigger );
            if( counter == null )
                counter = new Integer( 1 );
            else
                counter = new Integer( counter.intValue() + 1 );
            counters.put( trigger, counter );
        }
    }

    synchronized public void setMaxSize( final int size )
    {
        maxSize = size;
        purge();
    }

    synchronized public int getMaxSize()
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

            Iterator list = counters.entrySet().iterator();
            while( list.hasNext() )
            {
                Map.Entry entry = (Map.Entry) list.next();
                String type = (String) entry.getKey();
                Integer counts = (Integer) entry.getValue();
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
            eventList.clear();

        while( eventList.size() > maxSize )
        {
            eventList.remove(0);
        }
    }

    public List getAllAlarmEvents()
    {
        return eventList;
    }

    public Map getCounters()
    {
        return counters;
    }

    public void resetAllCounters()
    {
        counters.clear();
    }

    public int getActivateCounter()
    {
        Integer counter = (Integer) counters.get( Alarm.TRIGGER_ACTIVATION );
        if( counter == null )
        {
            return 0;
        }
        return counter.intValue();
    }

    public void resetActivateCounter()
    {
        counters.remove( Alarm.TRIGGER_ACTIVATION );
    }
}
