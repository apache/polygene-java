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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmCreationException;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmHistory;
import org.qi4j.library.alarm.AlarmListener;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmState;
import org.qi4j.library.alarm.AlarmTriggerException;
import org.qi4j.library.alarm.InternalAlarmException;

public class AlarmImpl
    implements Alarm, java.io.Serializable
{
    static final long serialVersionUID = 2L;

    private AlarmModel m_Model;
    private AlarmState m_State;
    private AlarmHistoryImpl m_History;
    private String m_Name;

    private Map m_Properties;
    private ArrayList m_Listeners = null;

    public AlarmImpl( AlarmModel model, String name )
        throws AlarmCreationException
    {
        if( model == null )
        {
            throw new AlarmCreationException( "model == null" );
        }

        if( name == null )
        {
            throw new AlarmCreationException( "name == null" );
        }
        name = name.trim();
        if( "".equals( name ) )
        {
            throw new AlarmCreationException( "name only contains white space" );
        }

        m_Name = name;
        m_Model = model;
        m_Properties = new HashMap();
        m_History = new AlarmHistoryImpl();
        m_State = m_Model.getAlarmModelProvider().createInitialState();
    }

    public AlarmModel getAlarmModel()
    {
        synchronized( this )
        {
            return m_Model;
        }
    }

    public void setAlarmModel( AlarmModel model )
    {
        synchronized( this )
        {
            m_Model = model;
        }
    }

    public void setProperty( String name, Object value )
    {
        synchronized( this )
        {
            if( value == null )
            {
                m_Properties.remove( name );
            }
            else
            {
                m_Properties.put( name, value );
            }
        }
    }

    public Object getProperty( String name )
    {
        synchronized( this )
        {
            Object value = m_Properties.get( name );
            if( value != null )
            {
                return value;
            }
            return m_Model.getDefaultProperties().get( name );
        }
    }

    public Map getProperties()
    {
        synchronized( this )
        {
            return new HashMap( m_Properties );
        }
    }

    public synchronized void addAlarmListener( AlarmListener listener )
    {
        synchronized( this )
        {
            ArrayList v;
            if( m_Listeners == null )
            {
                v = new ArrayList();
            }
            else
            {
                v = (ArrayList) m_Listeners.clone();
            }
            v.add( listener );
            m_Listeners = v;
        }
    }

    public synchronized void removeAlarmListener( AlarmListener listener )
    {
        if( listener == null )
        {
            return;
        }
        synchronized( this )
        {
            if( m_Listeners == null )
            {
                return;
            }
            ArrayList v = (ArrayList) m_Listeners.clone();
            v.remove( listener );
            m_Listeners = v;
        }
    }

    protected void fireAlarm( AlarmEvent event )
    {
        Collection all = m_Model.getAlarmListeners();
        if( all != null )
        {
            fireAlarm( all, event );
        }
        all = m_Listeners;
        if( all != null )
        {
            fireAlarm( all, event );
        }
    }

    private void fireAlarm( Collection listeners, AlarmEvent event )
    {
        Iterator list = listeners.iterator();
        while( list.hasNext() )
        {
            try
            {
                AlarmListener listener = (AlarmListener) list.next();
                listener.alarmFired( event );
            }
            catch( Exception e )
            {
                // ignore.
            }
        }
    }

    public String toString()
    {
        return "Alarm[" + getName() + " : " + m_State.getName() + "  : " + getDescription() + "]";
    }

    public void trigger( Object source, String trigger )
        throws AlarmTriggerException
    {
        AlarmEvent event;
        synchronized( this )
        {
            event = m_Model.getAlarmModelProvider().executeStateChange( source, this, trigger );
            if( event == null )
            {
                return;
            }
            m_State = event.getNewState();
            m_History.addEvent( event, trigger );
        }
        fireAlarm( event );
    }

    public void activate( Object source )
    {
        try
        {
            trigger( source, Alarm.TRIGGER_ACTIVATION );
        }
        catch( AlarmTriggerException e )
        {
            // Can not happen.
            throw new InternalAlarmException( "Exception occurred indicating a system corruption situation.", e );
        }
    }

    public void deactivate( Object source )
    {
        try
        {
            trigger( source, Alarm.TRIGGER_DEACTIVATION );
        }
        catch( AlarmTriggerException e )
        {
            // Can not happen.
            throw new InternalAlarmException( "Exception occurred indicating a system corruption situation.", e );
        }
    }

    public void acknowledge( Object source )
    {
        try
        {
            trigger( source, Alarm.TRIGGER_ACKNOWLEDGE );
        }
        catch( AlarmTriggerException e )
        {
            // Can not happen.
            throw new InternalAlarmException( "Exception occurred indicating a system corruption situation.", e );
        }
    }

    public AlarmHistory getHistory()
    {
        return m_History;
    }

    public AlarmState getState()
    {
        return m_State;
    }

    /**
     * Returns the Name of the Alarm.
     * This normally returns the human readable technical name of the Alarm.
     */
    public String getName()
    {
        return m_Name;
    }

    /**
     * Returns a Description of the Alarm.
     * This normally returns a full Description of the Alarm in the
     * default Locale.
     */
    public String getDescription()
    {
        return getDescription( null );
    }

    /**
     * Returns a Description of the Alarm.
     * This normally returns a full Description of the Alarm in the
     * Locale. If Locale is <code><b>null</b></code>, then the
     * default Locale is used.
     */
    public String getDescription( Locale locale )
    {
        if( locale == null )
        {
            locale = Locale.getDefault();
        }
        ResourceBundle rb = ResourceBundle.getBundle( "org.qi4j.library.alarm.user.AlarmDescriptions", locale );
        return rb.getString( getName() );
    }

    public void setCondition( boolean condition )
    {
        try
        {
            String trig = m_Model.getAlarmModelProvider().computeTrigger( m_State, condition );
            if( trig != null )
            {
                trigger( this, trig );
            }
        }
        catch( AlarmTriggerException e )
        {
            // Can not happen.
            throw new InternalAlarmException( "Exception occurred indicating a system corruption situation.", e );
        }
    }

    public boolean getCondition()
    {
        return m_Model.getAlarmModelProvider().computeCondition( m_State );
    }
}
