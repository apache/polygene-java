package org.qi4j.library.alarm;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;

import java.util.*;

public class AlarmMixin
    implements Alarm
{
    private static final long serialVersionUID = 2L;

    @Service
    private AlarmModel model;

    @Service
    private AlarmSystem alarmSystem;

    @This
    private Alarm me;

    @This
    private AlarmState state;

    @This
    private AlarmHistory history;

    public void setAttribute( String name, String value )
    {
        Map<String, String> properties = state.attributes().get();
        if( value == null )
        {
            properties.remove( name );
        }
        else
        {
            properties.put( name, value );
        }
        state.attributes().set( properties );
    }

    public String attribute( String name )
    {
        return state.attributes().get().get( name );
    }

    public List<String> attributeNames()
    {
        ArrayList<String> result = new ArrayList<String>();
        for( String name : state.attributes().get().keySet() )
        {
            result.add( name );
        }
        return result;
    }

    private void fireAlarm( AlarmEvent event )
    {
        // TODO: Should possibly just be delegated to AlarmSystem
        for( AlarmListener listener : alarmSystem.alarmListeners() )
        {
            try
            {
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
        return "Alarm[" + name() + " : " + state.currentStatus()
            .get()
            .name()
            .get() + "  : " + descriptionInDefaultLocale() + "]";
    }

    public void trigger( String trigger )
    {
        AlarmEvent event;
        synchronized( this )
        {
            event = model.evaluate( me, trigger );
            if( event == null )
            {
                return;
            }
            state.currentStatus().set( event.newStatus().get() );
            history.addEvent( event, trigger );
        }
        fireAlarm( event );
    }

    public void activate()
    {
        trigger( Alarm.TRIGGER_ACTIVATE );
    }

    public void deactivate()
    {
        trigger( Alarm.TRIGGER_DEACTIVATE );
    }

    public void acknowledge()
    {
        trigger( Alarm.TRIGGER_ACKNOWLEDGE );
    }

    public AlarmHistory history()
    {
        return history;
    }

    public AlarmStatus currentStatus()
    {
        return state.currentStatus().get();
    }

    /**
     * Returns the Name of the Alarm.
     * This normally returns the human readable technical name of the Alarm.
     */
    public String name()
    {
        return state.systemName().get();
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
        String trig = model.computeTrigger( state.currentStatus().get(), condition );
        if( trig != null )
        {
            trigger( trig );
        }
    }

    public boolean currentCondition()
    {
        return model.computeCondition( state.currentStatus().get() );
    }
}
