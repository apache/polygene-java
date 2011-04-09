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

package org.qi4j.library.alarm.providers.standard;

import java.text.MessageFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.Locale;
import java.util.ResourceBundle;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmState;

public abstract class GenericAlarmEvent extends EventObject
    implements AlarmEvent, java.io.Serializable
{

    static final long serialVersionUID = 2L;

    private Object m_TriggeredBy;
    private AlarmState m_OldState;
    private AlarmState m_NewState;
    private Date m_EventTime;

    protected GenericAlarmEvent( Object triggeredBy,
                                 Alarm source,
                                 AlarmState oldstate,
                                 AlarmState newstate,
                                 long time
    )
    {
        super( source );
        m_TriggeredBy = triggeredBy;
        m_OldState = oldstate;
        m_NewState = newstate;
        m_EventTime = new Date( time );
    }

    public Object getTriggeredBy()
    {
        return m_TriggeredBy;
    }

    public Alarm getAlarm()
    {
        return (Alarm) super.getSource();
    }

    public AlarmState getOldState()
    {
        return m_OldState;
    }

    public AlarmState getNewState()
    {
        return m_NewState;
    }

    public Date getEventTime()
    {
        return m_EventTime;
    }

    public String toString()
    {
        ResourceBundle rb = StandardModelProvider.getResourceBundle( null );
        String pattern = rb.getString( "EVENT_GENERIC_PART_TOSTRING" );
        String eventsource = getSource().toString();
        String oldstate = null;
        if( m_OldState != null )
        {
            oldstate = m_OldState.getName();
        }
        String newstate = null;
        if( m_NewState != null )
        {
            newstate = m_NewState.getName();
        }
        Object[] args = new Object[]{ eventsource, m_EventTime, oldstate, newstate };
        return MessageFormat.format( pattern, args );
    }

    public abstract String getResourceHead();

    /**
     * Returns the Name of the event.
     * This normally returns the human readable name of the Event, such as
     * activate, deactivate and acknowledge, in the default locale.
     */
    public String getName()
    {
        return getName( null );
    }

    /**
     * Returns the Name of the event.
     * This normally returns the human readable name of the Event, such as
     * activate, deactivate and acknowledge, in the given locale.
     */
    public String getName( Locale locale )
    {
        if( locale == null )
        {
            locale = Locale.getDefault();
        }
        ResourceBundle rb =
            ResourceBundle.getBundle( "org.qi4j.library.alarm.providers.standard.AlarmResources", locale );
        return rb.getString( getResourceHead() + "_NAME" );
    }

    /**
     * Returns a Description of the event.
     * This normally returns a brief description of the event type, but could/should
     * allow for Alarm specific descriptions for humans to be better informed.
     * The description is returned in the default Locale.
     *
     * @see #getDescription(Locale)
     */
    public String getDescription()
    {
        return getDescription( null );
    }

    /**
     * Returns a Description of the event in the specified locale.
     * This normally returns a brief description of the event type, but could/should
     * allow for Alarm specific descriptions for humans to be better informed.
     */
    public String getDescription( Locale locale )
    {
        if( locale == null )
        {
            locale = Locale.getDefault();
        }
        ResourceBundle rb =
            ResourceBundle.getBundle( "org.qi4j.library.alarm.providers.standard.AlarmResources", locale );
        return rb.getString( getResourceHead() + "_DESCRIPTION" );
    }
}
