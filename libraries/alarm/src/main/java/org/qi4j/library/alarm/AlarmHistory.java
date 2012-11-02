/*
 * Copyright 1996-2011 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.alarm;

import java.util.List;
import java.util.Map;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * History of an AlarmPoint.
 * AlarmPoint system <i>should</i> implement <code>AlarmHistory</code> classes to
 * record the events of an <code>AlarmPoint</code>.
 *
 * @author Niclas Hedhman
 */
@Mixins( AlarmHistory.AlarmHistoryMixin.class )
public interface AlarmHistory
{

    /**
     * Returns the newest recorded <code>AlarmEvent</code>.
     *
     * @return the last AlarmEvent in the buffer. This is the newest event.
     */
    AlarmEvent lastEvent();

    /**
     * Returns the oldest recorded <code>AlarmEvent</code>.
     *
     * @return the first AlarmEvent in the buffer. This is the oldest event.
     */
    AlarmEvent firstEvent();

    /**
     * Returns the recorded <code>AlarmEvent</code> at the given position
     * in the buffer.
     *
     * @param position the position in the buffer, counted from the beginning to obtain the AlarmEvent from.
     *                 0 means the first element.
     *
     * @return the recorded <code>AlarmEvent</code> at the given position in the buffer.
     */
    AlarmEvent eventAt( int position );

    /**
     * Returns the recorded <code>AlarmEvent</code> at the given position
     * in the buffer counted from the end.
     *
     * @param position the position in the buffer, counted from the end to obtain the AlarmEvent from.
     *                 0 means the last element.
     *
     * @return the recorded <code>AlarmEvent</code> at the given position in the buffer counted from the end.
     */
    AlarmEvent eventAtEnd( int position );

    /**
     * Returns a <code>java.util.List</code> of all recorded <code>AlarmEvents</code>.
     *
     * @return a <code>java.util.List</code> of all recorded <code>AlarmEvents</code>.
     */
    @UseDefaults
    Property<List<AlarmEvent>> allAlarmEvents();

    /**
     * The maximum size of the history buffer.
     * If the sizes shrinks, the oldest <code>AlarmEvents</code> should be removed
     * so that the number of stored events are equal to the new <i>MaxSize</i>.
     *
     * @return The maxSize Property instance.
     */
    @UseDefaults
    Property<Integer> maxSize();

    /**
     * Returns all the Counters of triggers.
     * Each time the <code>trigger()</code> method is called,
     * a Counter is incremented. That means that after the first
     * time the following the sequence is called
     * <pre>
     * <code>activate();
     * acknowledge();
     * deactivate();
     * </code></pre>, the Map contains <table>
     * <tr><td>(String) activate</td><td>(Integer) 1</td></tr>
     * <tr><td>(String) deactivate</td><td>(Integer) 1</td></tr>
     * <tr><td>(String) acknowledge</td><td>(Integer) 1</td></tr>
     * </table>
     *
     * @return all the Counters of triggers.
     */
    @UseDefaults
    Property<Map<String, Integer>> counters();

    /**
     * Resets all counters.
     */
    void resetAllCounters();

    /**
     * Returns the Counter of activate triggers.
     * This method will return the number of times the
     * <code>activate()</code> method and the <code>trigger()</code>
     * method with an activate trigger is called.
     *
     * @return the Counter of activate triggers.
     */
    int activateCounter();

    /**
     * Resets the Activate counter.
     */
    void resetActivateCounter();

    void addEvent( AlarmEvent event, String trigger );

    abstract class AlarmHistoryMixin
        implements AlarmHistory
    {
        @Override
        public AlarmEvent firstEvent()
        {
            List<AlarmEvent> eventList = allAlarmEvents().get();
            if( eventList.isEmpty() )
            {
                return null;
            }
            return eventList.get( 0 );
        }

        @Override
        public AlarmEvent lastEvent()
        {
            List<AlarmEvent> eventList = allAlarmEvents().get();
            if( eventList.isEmpty() )
            {
                return null;
            }
            return eventList.get( eventList.size() - 1 );
        }

        @Override
        public AlarmEvent eventAt( final int position )
        {
            List<AlarmEvent> eventList = allAlarmEvents().get();
            if( eventList.size() <= position || position < 0 )
            {
                return null;
            }
            return eventList.get( position );
        }

        @Override
        public AlarmEvent eventAtEnd( final int position )
        {
            List<AlarmEvent> eventList = allAlarmEvents().get();
            int size = eventList.size();
            if( size <= position || position < 0 )
            {
                return null;
            }
            return eventList.get( size - position - 1 );
        }

        @Override
        public void addEvent( AlarmEvent event, String trigger )
        {
            List<AlarmEvent> eventList = allAlarmEvents().get();
            eventList.add( event );
            purge();
            allAlarmEvents().set( eventList );
            Map<String, Integer> counters = counters().get();
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
            counters().set( counters );
        }

        @Override
        public String toString()
        {
            StringBuilder buf = new StringBuilder();
            buf.append( "history[maxsize=" );
            buf.append( maxSize().get() );
            buf.append( ", size=" );
            buf.append( allAlarmEvents().get().size() );

            for( Map.Entry<String, Integer> entry : counters().get().entrySet() )
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

        private void purge()
        {
            List<AlarmEvent> eventList = allAlarmEvents().get();
            Integer maxSize = maxSize().get();
            if( maxSize <= 0 )
            {
                eventList.clear();
            }

            while( eventList.size() > maxSize )
            {
                eventList.remove( 0 );
            }
        }

        @Override
        public void resetAllCounters()
        {
            Map<String, Integer> counters = counters().get();
            counters.clear();
            counters().set(counters);
        }

        @Override
        public int activateCounter()
        {
            Integer counter = counters().get().get( AlarmPoint.TRIGGER_ACTIVATE );
            if( counter == null )
            {
                return 0;
            }
            return counter;
        }

        @Override
        public void resetActivateCounter()
        {
            Map<String, Integer> counters = counters().get();
            counters.remove( AlarmPoint.TRIGGER_ACTIVATE );
            counters().set( counters );
        }
    }
}
