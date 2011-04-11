/*
 * Copyright 1996-2005 Niclas Hedhman.
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

/**
 * History of an Alarm.
 * Alarm system <i>should</i> implement <code>AlarmHistory</code> classes to
 * record the events of an <code>Alarm</code>.
 *
 * @author Niclas Hedhman
 */
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
    List<AlarmEvent> getAllAlarmEvents();

    /**
     * Sets the maximum size of the history buffer.
     * If the sizes shrinks, the oldest <code>AlarmEvents</code> should be removed
     * so that the number of stored events are equal to the new <i>MaxSize</i>.
     *
     * @param size the maximum size of the history buffer.
     */
    void setMaxSize( int size );

    /**
     * Returns the maximum size of the buffer.
     *
     * @return the maximum size of the buffer.
     */
    int maxSize();

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
    Map<String, Integer> counters();

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
}
