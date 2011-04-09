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
    static final long serialVersionUID = 2L;

    private final ArrayList m_EventList;
    private int m_MaxSize = 30;
    private HashMap m_Counters;

    public AlarmHistoryImpl()
    {
        m_EventList = new ArrayList();
        m_Counters = new HashMap();
    }

    public AlarmEvent getFirst()
    {
        synchronized( m_EventList )
        {
            if( m_EventList.size() == 0 )
                return null;
            return (AlarmEvent) m_EventList.get(0);
        }
    }

    public AlarmEvent getLast()
    {
        synchronized(m_EventList)
        {
            if( m_EventList.size() == 0 )
                return null;
            return (AlarmEvent) m_EventList.get(m_EventList.size()-1);
        }
    }

    public AlarmEvent getAt( final int position )
    {
        synchronized( this )
        {
            if( m_EventList.size() <= position || position < 0 )
                return null;
            return (AlarmEvent) m_EventList.get(position);
        }
    }

    public AlarmEvent getAtFromLast( final int position )
    {
        synchronized( m_EventList )
        {
            int size = m_EventList.size();
            if( size <= position || position < 0 )
                return null;
            return (AlarmEvent) m_EventList.get( size-position-1 );
        }
    }

    public void addEvent( final AlarmEvent event, final String trigger )
    {
        synchronized( this )
        {
            m_EventList.add(event);
            purge();
            Integer counter = (Integer) m_Counters.get( trigger );
            if( counter == null )
                counter = new Integer( 1 );
            else
                counter = new Integer( counter.intValue() + 1 );
            m_Counters.put( trigger, counter );
        }
    }

    synchronized public void setMaxSize( final int size )
    {
        m_MaxSize = size;
        purge();
    }

    synchronized public int getMaxSize()
    {
        return m_MaxSize;
    }

    public String toString()
    {
        synchronized(m_EventList)
        {
            StringBuffer buf = new StringBuffer();
            buf.append( "history[maxsize=" );
            buf.append( m_MaxSize );
            buf.append( ", size=" );
            buf.append( m_EventList.size() );

            Iterator list = m_Counters.entrySet().iterator();
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
        if( m_MaxSize <= 0 )
            m_EventList.clear();

        while( m_EventList.size() > m_MaxSize )
        {
            m_EventList.remove(0);
        }
    }

    public List getAllAlarmEvents()
    {
        return m_EventList;
    }

    public Map getCounters()
    {
        return m_Counters;
    }

    public void resetAllCounters()
    {
        m_Counters.clear();
    }

    public int getActivateCounter()
    {
        Integer counter = (Integer) m_Counters.get( Alarm.TRIGGER_ACTIVATION );
        if( counter == null )
        {
            return 0;
        }
        return counter.intValue();
    }

    public void resetActivateCounter()
    {
        m_Counters.remove( Alarm.TRIGGER_ACTIVATION );
    }
}
