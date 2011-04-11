/*
 * Copyright 2005 Niclas Hedhman.
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
package org.qi4j.library.alarm.providers.standard.tests;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmHistory;
import org.qi4j.library.alarm.AlarmListener;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmState;

public class AlarmTestImpl
    implements Alarm
{
    private AlarmHistory history;
    private AlarmModel model;
    private boolean condition;
    private AlarmState state;
    private Map properties;
    private String name;

    public AlarmTestImpl( AlarmModel model, String name )
    {
        this.model = model;
        properties = new HashMap();
        history = new TestAlarmHistory();
        condition = false;
        state = model.alarmModelProvider().createInitialState();
        this.name = name;
    }

    public AlarmModel alarmModel()
    {
        return model;
    }

    public void setAlarmModel( AlarmModel model )
    {
        this.model = model;
    }

    public void trigger( Object source, String trig )
    {

    }

    public void activate( Object source )
    {
        trigger( source, "activate" );
    }

    public void deactivate( Object source )
    {
        trigger( source, "deactivate" );
    }

    public void acknowledge( Object source )
    {
        trigger( source, "acknowledge" );
    }

    public boolean currentCondition()
    {
        return condition;
    }

    public void updateCondition( boolean condition )
    {
        this.condition = condition;
    }

    public AlarmState alarmState()
    {
        return state;
    }

    public AlarmHistory history()
    {
        return history;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public Object getProperty( String name )
    {
        return properties.get( name );
    }

    public void setProperty( String name, Object value )
    {
        properties.put( name, value );
    }

    public String name()
    {
        return name;
    }

    public String descriptionInDefaultLocale()
    {
        return "Some dummy description.";
    }

    public String description( Locale locale )
    {
        return "Some dummy description.";
    }

    public void addAlarmListener( AlarmListener listener )
    {
    }

    public void removeAlarmListener( AlarmListener listener )
    {
    }
}
