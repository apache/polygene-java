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

import java.util.Date;
import java.util.Locale;

/**
 * Event for indicating change of AlarmState of an Alarm.
 *
 * @author Niclas Hedhman
 */
public interface AlarmEvent
{

    /**
     * Returns the source of the trigger.
     *
     * @return The Object that created the trigger.
     */
    Object getTriggeredBy();

    /**
     * Returns the Alarm that generated the event.
     * Also known as <code>getSource</code> under the normal Java Event Model
     * but casted to the Alarm type.
     *
     * @return the Alarm causing this event.
     */
    Alarm getAlarm();

    /**
     * Returns the AlarmState prior to the Event.
     *
     * @return the old AlarmState prior to this event.
     */
    AlarmState getOldState();

    /**
     * Returns the AlarmState after the Event.
     *
     * @return the new AlarmState of the Alarm after this event.
     */
    AlarmState getNewState();

    /**
     * Returns the Time when the event occurred.
     *
     * @return the timestamp when this event occurred.
     */
    Date getEventTime();

    /**
     * Returns the Name of the event.
     * This normally returns the human readable name of the Event, such as
     * activate, deactivate and acknowledge, in the default locale.
     *
     * @return the name of this event in the default locale.
     */
    String getName();

    /**
     * Returns the Name of the event.
     * This normally returns the human readable name of the Event, such as
     * activate, deactivate and acknowledge, in the given locale.
     *
     * @param locale the locale that the name should be returned in.
     *
     * @return the name of the event in the given locale.
     */
    String getName( Locale locale );

    /**
     * Returns a Description of the event.
     * This normally returns a brief description of the event type, but could/should
     * allow for Alarm specific descriptions for humans to be better informed.
     * The description is returned in the default Locale.
     *
     * @return the description of the event in the default locale.
     *
     * @see #getDescription(Locale)
     */
    String getDescription();

    /**
     * Returns a Description of the event in the specified locale.
     * This normally returns a brief description of the event type, but could/should
     * allow for Alarm specific descriptions for humans to be better informed.
     *
     * @param locale the locale that the description should be returned in.
     *
     * @return the description of the event in the given locale.
     */
    String getDescription( Locale locale );
}
