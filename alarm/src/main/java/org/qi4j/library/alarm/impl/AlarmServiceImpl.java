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
import java.util.Iterator;
import java.util.List;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmCreationException;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmListener;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmService;

/**
 * AlarmService implementation.
 */
public class AlarmServiceImpl
    implements AlarmService, AlarmListener
{

    private ArrayList alarmListeners;
    private final ArrayList models;
    private AlarmModel defaultModel;

    public AlarmServiceImpl()
    {
        models = new ArrayList();
        alarmListeners = new ArrayList();
    }

    /**
     * Returns all the AlarmModels that has been installed.
     */
    public AlarmModel[] getAlarmModels()
    {
        synchronized( models )
        {
            AlarmModel[] am = new AlarmModel[ models.size()];
            models.toArray( am );
            return am;
        }
    }

    /**
     * Returns the default AlarmModel.
     */
    public AlarmModel getDefaultAlarmModel()
    {
        synchronized( this )
        {
            return defaultModel;
        }
    }

    /**
     * Sets the default AlarmModel.
     * Changes the default AlarmModel. All Alarms belonging
     * to the old default AlarmModel will be re-assigned to the
     * new default AlarmModel, for easy change of AlarmModels.
     * When the AlarmModel is changed, the Alarms will receieve
     * an event indicating that this has happened, and will
     * reset their states to NormalState, to avoid illegal states.
     */
    public void setDefaultAlarmModel( AlarmModel model )
    {
        synchronized( this )
        {
            AlarmModel oldModel = defaultModel;
            if( oldModel != null )
            {
                oldModel.newDefaultModelSet( model );
            }
            defaultModel = model;
        }
    }

    /**
     * Creates an Alarm with the default AlarmModel.
     */
    public Alarm createAlarm( String name )
        throws AlarmCreationException
    {
        return getDefaultAlarmModel().createAlarm( name );
    }

    /**
     * Register AlarmListener to recieve <code>AlarmEvents</code> from all
     * <code>Alarms</code> managed by this <code>AlarmService</code>.
     */
    public void addAlarmListener( AlarmListener listener )
    {
        //noinspection SynchronizeOnNonFinalField
        synchronized( alarmListeners )
        {
            ArrayList clone = (ArrayList) alarmListeners.clone();
            clone.add( listener );
            alarmListeners = clone;
        }
    }

    /**
     * Remove the <code>AlarmListener</code> from the <code>AlarmService</code>.
     */
    public void removeAlarmListener( AlarmListener listener )
    {
        //noinspection SynchronizeOnNonFinalField
        synchronized( alarmListeners )
        {
            ArrayList clone = (ArrayList) alarmListeners.clone();
            clone.remove( listener );
            alarmListeners = clone;
        }
    }

    /**
     * Returns a list of all Alarms registered to the service.
     */
    public List getAlarms()
    {
        ArrayList result = new ArrayList();
        AlarmModel[] models = getAlarmModels();
        for( int i = 0; i < models.length; i++ )
        {
            List c = models[ i ].getAlarms();
            result.addAll( c );
        }
        return result;
    }

    public List getAlarmListeners()
    {
        synchronized( alarmListeners )
        {
            return alarmListeners;
        }
    }

    public void alarmFired( AlarmEvent event )
    {
        Iterator list;
        //noinspection SynchronizeOnNonFinalField
        synchronized( alarmListeners )
        {
            list = alarmListeners.iterator();
        }
        while( list.hasNext() )
        {
            AlarmListener listener = (AlarmListener) list.next();
            try
            {
                listener.alarmFired( event );
            }
            catch( Exception e )
            {
                // TODO: Utilize a logger system instead.
                System.err.println( "Exception in AlarmListener: " + listener );
                e.printStackTrace();
            }
        }
    }

    public void addAlarmModel( AlarmModel model )
    {
        synchronized( models )
        {
            models.add( model );
            if( defaultModel == null )
            {
                setDefaultAlarmModel( model );
            }
            model.addAlarmListener( this
            ); // TODO NH: Should we really listen on all AlarmModels or only on the default one?
        }
    }

    public void removeAlarmModel( AlarmModel model )
    {
        synchronized( models )
        {
            models.remove( model );
            model.removeAlarmListener( this );
            if( models.size() == 0 )
            {
                setDefaultAlarmModel( null );
            }
            else if( defaultModel.equals( model ) && !models.contains( model ) )
            {
                int lastIndex = models.size() - 1;
                AlarmModel lastModel = (AlarmModel) models.get( lastIndex );
                setDefaultAlarmModel( lastModel );
            }
        }
    }
}
