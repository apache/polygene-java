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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import org.qi4j.library.alarm.AlarmModelProvider;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmCreationException;
import org.qi4j.library.alarm.AlarmListener;

/**
 *
 */
public class AlarmModelImpl
    implements AlarmModel
{
    private AlarmModelProvider provider;
    private ArrayList listeners;
    private HashMap properties;
    private final WeakHashMap alarms;

    public AlarmModelImpl( AlarmModelProvider provider )
    {
        alarms = new WeakHashMap();
        properties = new HashMap();
        this.provider = provider;
    }

    /** Called when the default AlarmModel is changed.
     * This AlarmModel has been the default AlarmModel, and now it is being changed
     * to another AlarmModel. This instance must transfer all the registered alarms
     * to the new AlarmModel.
     **/
    public void newDefaultModelSet( AlarmModel newDefaultModel )
    {
        Iterator list;
        synchronized( alarms )
        {
            list = alarms.keySet().iterator();
        }
        while( list.hasNext() )
        {
            Alarm alarm = (Alarm) list.next();
            newDefaultModel.registerAlarm( alarm );
            list.remove();
        }
    }

    public void registerAlarm( Alarm alarm )
    {
        synchronized( alarms )
        {
            alarms.put( alarm, new Date() );
        }
    }

    public String getName()
    {
        return provider.getName();
    }

    public String getDescription()
    {
        return getDescription( null );
    }

    public String getDescription( Locale locale )
    {
        return provider.getDescription( locale );
    }

    public Alarm createAlarm( String name )
        throws AlarmCreationException
    {
        Alarm alarm = new AlarmImpl( this, name );
        registerAlarm( alarm );
        return alarm;
    }

    public List getAlarms()
    {
        ArrayList result = new ArrayList();
        Iterator list;
        synchronized( alarms )
        {
            list = alarms.keySet().iterator();
        }
        while( list.hasNext() )
        {
            Alarm alarm = (Alarm) list.next();
            result.add( alarm );
        }
        return result;
    }

    public String[] getAlarmTriggers()
    {
        return getAlarmModelProvider().getAlarmTriggers();
    }

    public AlarmModelProvider getAlarmModelProvider()
    {
        return provider;
    }

    /** Register AlarmListener to recieve <code>AlarmEvents</code> from all
     * <code>Alarms</code> managed by this <code>AlarmModel</code>.
     */
    public void addAlarmListener( AlarmListener listener )
    {
        synchronized( this )
        {
            ArrayList v;
            if( listeners == null )
                v = new ArrayList();
            else
                v = (ArrayList) listeners.clone();
            v.add( listener );
            listeners = v;
        }
    }

    /** Remove the <code>AlarmListener</code> from the <code>AlarmModel</code>.
     */
    public void removeAlarmListener( AlarmListener listener )
    {
        if( listeners == null )
        {
            return;
        }
        synchronized( this )
        {
            ArrayList v = (ArrayList) listeners.clone();
            v.remove( listener );
            listeners = v;
        }
    }

    public List getAlarmListeners()
    {
        synchronized( this )
        {
            if( listeners == null )
            {
                return new ArrayList();
            }
            return listeners;
        }
    }

    /** Adds a new <i>Property</i> to <strong>all</strong> <code>Alarms</code>.
     * The <code>defaultvalue</code> will be added to all present and future
     * <code>Alarms</code> created through this AlarmModel. In case any existing
     * <code>Alarms</code> already have this property defined, the existing value
     * should not be overwritten.
     */
    public void addProperty( String name, String defaultValue )
    {
        properties.put( name, defaultValue );
    }

    /** Removes the <i>Property</i> from all <code>Alarms</code>.
     */
    public void removeProperty( String name )
    {
        properties.remove( name );
    }

    /** Returns a <code>java.util.Map</code> of all global default <i>Properties</i>.
     */
    public Map getDefaultProperties()
    {
        return properties;
    }
}
