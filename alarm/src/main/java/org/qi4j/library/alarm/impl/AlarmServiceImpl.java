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

    private ArrayList m_AlarmListeners;
    private final ArrayList m_Models;
    private AlarmModel m_DefaultModel;

    public AlarmServiceImpl()
    {
        m_Models = new ArrayList();
        m_AlarmListeners = new ArrayList();
    }

    /**
     * Returns all the AlarmModels that has been installed.
     */
    public AlarmModel[] getAlarmModels()
    {
        synchronized( m_Models )
        {
            AlarmModel[] am = new AlarmModel[m_Models.size()];
            m_Models.toArray( am );
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
            return m_DefaultModel;
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
            AlarmModel oldModel = m_DefaultModel;
            if( oldModel != null )
            {
                oldModel.newDefaultModelSet( model );
            }
            m_DefaultModel = model;
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
        synchronized( m_AlarmListeners )
        {
            ArrayList clone = (ArrayList) m_AlarmListeners.clone();
            clone.add( listener );
            m_AlarmListeners = clone;
        }
    }

    /**
     * Remove the <code>AlarmListener</code> from the <code>AlarmService</code>.
     */
    public void removeAlarmListener( AlarmListener listener )
    {
        //noinspection SynchronizeOnNonFinalField
        synchronized( m_AlarmListeners )
        {
            ArrayList clone = (ArrayList) m_AlarmListeners.clone();
            clone.remove( listener );
            m_AlarmListeners = clone;
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
        synchronized( m_AlarmListeners )
        {
            return m_AlarmListeners;
        }
    }

    public void alarmFired( AlarmEvent event )
    {
        Iterator list;
        //noinspection SynchronizeOnNonFinalField
        synchronized( m_AlarmListeners )
        {
            list = m_AlarmListeners.iterator();
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
        synchronized( m_Models )
        {
            m_Models.add( model );
            if( m_DefaultModel == null )
            {
                setDefaultAlarmModel( model );
            }
            model.addAlarmListener( this
            ); // TODO NH: Should we really listen on all AlarmModels or only on the default one?
        }
    }

    public void removeAlarmModel( AlarmModel model )
    {
        synchronized( m_Models )
        {
            m_Models.remove( model );
            model.removeAlarmListener( this );
            if( m_Models.size() == 0 )
            {
                setDefaultAlarmModel( null );
            }
            else if( m_DefaultModel.equals( model ) && !m_Models.contains( model ) )
            {
                int lastIndex = m_Models.size() - 1;
                AlarmModel lastModel = (AlarmModel) m_Models.get( lastIndex );
                setDefaultAlarmModel( lastModel );
            }
        }
    }
}
