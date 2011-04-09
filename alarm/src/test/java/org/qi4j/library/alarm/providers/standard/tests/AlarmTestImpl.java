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
    private AlarmHistory m_History;
    private AlarmModel m_Model;
    private boolean m_Condition;
    private AlarmState m_State;
    private Map m_Properties;
    private String m_Name;

    public AlarmTestImpl( AlarmModel model, String name )
    {
        m_Model = model;
        m_Properties = new HashMap();
        m_History = new TestAlarmHistory();
        m_Condition = false;
        m_State = model.getAlarmModelProvider().createInitialState();
        m_Name = name;
    }

    public AlarmModel getAlarmModel()
    {
        return m_Model;
    }

    public void setAlarmModel( AlarmModel model )
    {
        m_Model = model;
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

    public boolean getCondition()
    {
        return m_Condition;
    }

    public void setCondition( boolean condition )
    {
        m_Condition = condition;
    }

    public AlarmState getState()
    {
        return m_State;
    }

    public AlarmHistory getHistory()
    {
        return m_History;
    }

    public Map getProperties()
    {
        return m_Properties;
    }

    public Object getProperty( String name )
    {
        return m_Properties.get( name );
    }

    public void setProperty( String name, Object value )
    {
        m_Properties.put( name, value );
    }

    public String getName()
    {
        return m_Name;
    }

    public String getDescription()
    {
        return "Some dummy description.";
    }

    public String getDescription( Locale locale )
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
