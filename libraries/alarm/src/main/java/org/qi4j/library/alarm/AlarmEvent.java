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

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueComposite;

/**
 * Event for indicating change of AlarmStatus of an AlarmPoint.
 *
 * @author Niclas Hedhman
 */
@Mixins( AlarmEvent.Mixin.class )
public interface AlarmEvent
    extends ValueComposite
{
    /**
     * Returns the identity of the AlarmPoint that generated the event.
     *
     * @return the AlarmPoint causing this event.
     */
    Property<String> alarmIdentity();

    /**
     * Returns the AlarmStatus prior to the Event.
     *
     * @return the old AlarmStatus prior to this event.
     */
    Property<AlarmStatus> oldStatus();

    /**
     * Returns the AlarmStatus after the Event.
     *
     * @return the new AlarmStatus of the AlarmPoint after this event.
     */
    Property<AlarmStatus> newStatus();

    /**
     * Returns the Time when the event occurred.
     *
     * @return the timestamp when this event occurred.
     */
    Property<Date> eventTime();

    /**
     * Returns the Name of the event.
     * This normally returns the human readable name of the Event, such as
     * activate, deactivate and acknowledge, in the default locale.
     *
     * @return the name of this event in the default locale.
     */
    Property<String> systemName();

    /**
     * Returns the Name of the event.
     * This normally returns the human readable name of the Event, such as
     * activate, deactivate and acknowledge, in the given locale.
     *
     * @param locale the locale that the name should be returned in.
     *
     * @return the name of the event in the given locale.
     */
    String name( Locale locale );

    /**
     * Returns a Description of the event in the specified locale.
     * This normally returns a brief description of the event type, but could/should
     * allow for AlarmPoint specific descriptions for humans to be better informed.
     *
     * @param locale the locale that the description should be returned in.
     *
     * @return the description of the event in the given locale.
     */
    String description( Locale locale );

    abstract class Mixin
        implements AlarmEvent
    {
        @Service
        private AlarmModel model;

        @Override
        public String name( Locale locale )
        {
            ResourceBundle bundle = ResourceBundle.getBundle( ((ServiceComposite) model).identity().get(), locale );
            return bundle.getString( systemName().get() );
        }

        @Override
        public String description( Locale locale )
        {
            ResourceBundle bundle = ResourceBundle.getBundle( ((ServiceComposite) model).identity().get(), locale );
            String eventDescriptionId = "EVENT_" + systemName().get().toUpperCase() + "_DESCRIPTION";
            return bundle.getString( eventDescriptionId );
        }
    }
}
