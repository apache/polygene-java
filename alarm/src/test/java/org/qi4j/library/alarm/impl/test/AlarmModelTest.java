/*
 * Copyright 2005 Niclas Hedhman.
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
package org.qi4j.library.alarm.impl.test;

import junit.framework.TestCase;

import java.util.List;
import java.util.Locale;
import org.qi4j.library.alarm.AlarmModelProvider;
import org.qi4j.library.alarm.AlarmListener;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.providers.simple.SimpleModelProvider;
import org.qi4j.library.alarm.impl.AlarmModelImpl;
import org.qi4j.library.alarm.impl.AlarmImpl;

public class AlarmModelTest extends TestCase
    implements AlarmListener
{
    private AlarmModelProvider m_Provider;
    private AlarmModelImpl     m_Model;
    private int m_Counter;

    public void setUp()
        throws Exception
    {
        m_Provider = new SimpleModelProvider();
        m_Model = new AlarmModelImpl( m_Provider );
    }

    public void testAlarmRegistrations()
        throws Exception
    {
        List alarms =  m_Model.getAlarms();
        assertEquals( "Registered Alarms.", 0, alarms.size() );
        Alarm alarm = new AlarmImpl( m_Model, "TestAlarm" );
        alarms =  m_Model.getAlarms();
        assertEquals( "Registered Alarms.", 0, alarms.size() );
        m_Model.registerAlarm( alarm );
        alarms =  m_Model.getAlarms();
        assertEquals( "Registered Alarms.", 1, alarms.size() );
    }

    public void testName()
    {
        String name = m_Model.getName();
        assertEquals( "org.qi4j.library.alarm.model.simple", name );
    }

    public void testDescription()
        throws Exception
    {
        String descr1 = m_Model.getDescription();
        String text = "Simple AlarmModel for \"activation\" and \"deactivation\" events, and \"Normal\" and \"Activated\" states.";
        assertEquals( text, descr1 );

        String descr2 = m_Model.getDescription( null );
        assertEquals( text, descr2 );

        Locale english = Locale.UK;
        String descr3 = m_Model.getDescription( english );
        assertEquals( text, descr3 );

        Locale swedish = new Locale( "sv"  );
        String descr4 = m_Model.getDescription( swedish );
        assertEquals( "Enkel AlarmModel f\u00F6r \"utl\u00F6sning\" och \"fr\u00E5ng\u00E5ng\" h\u00E4ndelser, samt \"normalt\" och \"utl\u00F6st\" tillst\u00E5nd.", descr4 );
    }

    public void testAlarmTriggers()
        throws Exception
    {
        String[] triggers = m_Model.getAlarmTriggers();
        int mask = 0;
        for( int i = 0; i < triggers.length; i++ )
        {
            if( Alarm.TRIGGER_ACTIVATION.equals( triggers[ i ] ) )
                mask = mask + 2;
            else if( Alarm.TRIGGER_DEACTIVATION.equals( triggers[ i ] ) )
                mask = mask + 4;
            else
                mask = mask + 8;
        }
        assertEquals( "Incorrect set of triggers.", 6, mask );
    }

    public void testListeners()
        throws Exception
    {
        m_Model.removeAlarmListener( null );
        m_Model.removeAlarmListener( this );
        m_Model.removeAlarmListener( null );
        m_Model.addAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 1, m_Model.getAlarmListeners().size() );
        m_Model.addAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 2, m_Model.getAlarmListeners().size() );
        m_Model.addAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 3, m_Model.getAlarmListeners().size() );
        m_Model.removeAlarmListener( null );
        assertEquals( "Some problem with Listener registrations", 3, m_Model.getAlarmListeners().size() );
        m_Model.removeAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 2, m_Model.getAlarmListeners().size() );
        m_Model.removeAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 1, m_Model.getAlarmListeners().size() );
        m_Model.removeAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 0, m_Model.getAlarmListeners().size() );
        m_Model.removeAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 0, m_Model.getAlarmListeners().size() );
    }

    public void testProperties()
        throws Exception
    {
        m_Model.addProperty( "my property", "my value" );
        Alarm alarm = m_Model.createAlarm( "my alarm" );
        String prop = (String) alarm.getProperty( "my property" );
        assertEquals( "Property setting.", "my value", prop );
        m_Model.addProperty( "my other property", "my other value" );
        prop = (String) alarm.getProperty( "my property" );
        assertEquals( "Property setting.", "my value", prop );
        prop = (String) alarm.getProperty( "my other property" );
        assertEquals( "Property setting.", "my other value", prop );

        m_Model.removeProperty( "my property" );
        prop = (String) alarm.getProperty( "my property" );
        assertEquals( "Property setting.", null, prop );

        alarm.setProperty( "my other property", "this value" );
        prop = (String) alarm.getProperty( "my other property" );
        assertEquals( "Property setting.", "this value", prop );

        alarm.setProperty( "my other property", null );
        prop = (String) alarm.getProperty( "my other property" );
        assertEquals( "Property setting.", "my other value", prop );

        alarm.setProperty( "my other property", "this value" );
        m_Model.removeProperty( "my other property" );
        prop = (String) alarm.getProperty( "my other property" );
        assertEquals( "Property setting.", "this value", prop );
    }

    public void testNewDefaultModel()
        throws Exception
    {
        List alarms = m_Model.getAlarms();
        assertEquals( "Alarms registered.", 0, alarms.size() );
        Alarm alarm1 = m_Model.createAlarm( "my alarm 1" );
        Alarm alarm2 = m_Model.createAlarm( "my alarm 2" );
        Alarm alarm3 = m_Model.createAlarm( "my alarm 3" );
        Alarm alarm4 = m_Model.createAlarm( "my alarm 4" );
        Alarm alarm5 = m_Model.createAlarm( "my alarm 5" );
        alarms = m_Model.getAlarms();
        assertEquals( "Alarms registered.", 5, alarms.size() );
        AlarmModel model = new AlarmModelImpl( m_Provider );
        m_Model.newDefaultModelSet( model );
        alarms = m_Model.getAlarms();
        assertEquals( "Alarms registered.", 0, alarms.size() );
        alarms = model.getAlarms();
        assertEquals( "Alarms registered.", 5, alarms.size() );

        alarm1.addAlarmListener( this );
        alarm2.addAlarmListener( this );
        alarm3.addAlarmListener( this );
        alarm4.addAlarmListener( this );
        alarm5.addAlarmListener( this );
        alarm1.setCondition( true );
        alarm2.setCondition( true );
        alarm3.setCondition( true );
        alarm4.setCondition( true );
        alarm5.setCondition( true );
        alarm1.removeAlarmListener( this );
        alarm2.removeAlarmListener( this );
        alarm3.removeAlarmListener( this );
        alarm4.removeAlarmListener( this );
        alarm5.removeAlarmListener( this );
        assertEquals( "Wrong number of AlarmListener calls.", 5, m_Counter );
    }

    public void alarmFired( AlarmEvent event )
    {
        m_Counter++;
    }
}
