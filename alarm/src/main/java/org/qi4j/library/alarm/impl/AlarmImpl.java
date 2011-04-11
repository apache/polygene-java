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
    private static final long serialVersionUID = 2L;

    private AlarmModel model;
    private AlarmState state;
    private AlarmHistoryImpl history;
    private String name;

    private Map<String,Object> properties;
    private ArrayList listeners = null;

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

        this.name = name;
        this.model = model;
        properties = new HashMap<String,Object>();
        history = new AlarmHistoryImpl();
        state = this.model.alarmModelProvider().createInitialState();
    }

    public AlarmModel alarmModel()
    {
        synchronized( this )
        {
            return model;
        }
    }

    public void setAlarmModel( AlarmModel model )
    {
        synchronized( this )
        {
            this.model = model;
        }
    }

    public void setProperty( String name, Object value )
    {
        synchronized( this )
        {
            if( value == null )
            {
                properties.remove( name );
            }
            else
            {
                properties.put( name, value );
            }
        }
    }

    public Object getProperty( String name )
    {
        synchronized( this )
        {
            Object value = properties.get( name );
            if( value != null )
            {
                return value;
            }
            return model.defaultProperties().get( name );
        }
    }

    public Map<String, Object> getProperties()
    {
        synchronized( this )
        {
            return new HashMap<String,Object>( properties );
        }
    }

    public synchronized void addAlarmListener( AlarmListener listener )
    {
        synchronized( this )
        {
            ArrayList v;
            if( listeners == null )
            {
                v = new ArrayList();
            }
            else
            {
                v = (ArrayList) listeners.clone();
            }
            v.add( listener );
            listeners = v;
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
            if( listeners == null )
            {
                return;
            }
            ArrayList v = (ArrayList) listeners.clone();
            v.remove( listener );
            listeners = v;
        }
    }

    protected void fireAlarm( AlarmEvent event )
    {
        Collection all = model.alarmListeners();
        if( all != null )
        {
            fireAlarm( all, event );
        }
        all = listeners;
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
        return "Alarm[" + name() + " : " + state.getName() + "  : " + descriptionInDefaultLocale() + "]";
    }

    public void trigger( Object source, String trigger )
        throws AlarmTriggerException
    {
        AlarmEvent event;
        synchronized( this )
        {
            event = model.alarmModelProvider().executeStateChange( source, this, trigger );
            if( event == null )
            {
                return;
            }
            state = event.newState();
            history.addEvent( event, trigger );
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

    public AlarmHistory history()
    {
        return history;
    }

    public AlarmState alarmState()
    {
        return state;
    }

    /**
     * Returns the Name of the Alarm.
     * This normally returns the human readable technical name of the Alarm.
     */
    public String name()
    {
        return name;
    }

    /**
     * Returns a Description of the Alarm.
     * This normally returns a full Description of the Alarm in the
     * default Locale.
     */
    public String descriptionInDefaultLocale()
    {
        return description( null );
    }

    /**
     * Returns a Description of the Alarm.
     * This normally returns a full Description of the Alarm in the
     * Locale. If Locale is <code><b>null</b></code>, then the
     * default Locale is used.
     */
    public String description( Locale locale )
    {
        if( locale == null )
        {
            locale = Locale.getDefault();
        }
        ResourceBundle rb = ResourceBundle.getBundle( "org.qi4j.library.alarm.user.AlarmDescriptions", locale );
        return rb.getString( name() );
    }

    public void updateCondition( boolean condition )
    {
        try
        {
            String trig = model.alarmModelProvider().computeTrigger( state, condition );
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

    public boolean currentCondition()
    {
        return model.alarmModelProvider().computeCondition( state );
    }
}
