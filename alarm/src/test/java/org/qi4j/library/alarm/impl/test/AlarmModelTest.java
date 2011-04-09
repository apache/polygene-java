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
    private AlarmModelProvider provider;
    private AlarmModelImpl model;
    private int counter;

    public void setUp()
        throws Exception
    {
        provider = new SimpleModelProvider();
        model = new AlarmModelImpl( provider );
    }

    public void testAlarmRegistrations()
        throws Exception
    {
        List alarms =  model.getAlarms();
        assertEquals( "Registered Alarms.", 0, alarms.size() );
        Alarm alarm = new AlarmImpl( model, "TestAlarm" );
        alarms =  model.getAlarms();
        assertEquals( "Registered Alarms.", 0, alarms.size() );
        model.registerAlarm( alarm );
        alarms =  model.getAlarms();
        assertEquals( "Registered Alarms.", 1, alarms.size() );
    }

    public void testName()
    {
        String name = model.getName();
        assertEquals( "org.qi4j.library.alarm.model.simple", name );
    }

    public void testDescription()
        throws Exception
    {
        String descr1 = model.getDescription();
        String text = "Simple AlarmModel for \"activation\" and \"deactivation\" events, and \"Normal\" and \"Activated\" states.";
        assertEquals( text, descr1 );

        String descr2 = model.getDescription( null );
        assertEquals( text, descr2 );

        Locale english = Locale.UK;
        String descr3 = model.getDescription( english );
        assertEquals( text, descr3 );

        Locale swedish = new Locale( "sv"  );
        String descr4 = model.getDescription( swedish );
        assertEquals( "Enkel AlarmModel f\u00F6r \"utl\u00F6sning\" och \"fr\u00E5ng\u00E5ng\" h\u00E4ndelser, samt \"normalt\" och \"utl\u00F6st\" tillst\u00E5nd.", descr4 );
    }

    public void testAlarmTriggers()
        throws Exception
    {
        String[] triggers = model.getAlarmTriggers();
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
        model.removeAlarmListener( null );
        model.removeAlarmListener( this );
        model.removeAlarmListener( null );
        model.addAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 1, model.getAlarmListeners().size() );
        model.addAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 2, model.getAlarmListeners().size() );
        model.addAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 3, model.getAlarmListeners().size() );
        model.removeAlarmListener( null );
        assertEquals( "Some problem with Listener registrations", 3, model.getAlarmListeners().size() );
        model.removeAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 2, model.getAlarmListeners().size() );
        model.removeAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 1, model.getAlarmListeners().size() );
        model.removeAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 0, model.getAlarmListeners().size() );
        model.removeAlarmListener( this );
        assertEquals( "Some problem with Listener registrations", 0, model.getAlarmListeners().size() );
    }

    public void testProperties()
        throws Exception
    {
        model.addProperty( "my property", "my value" );
        Alarm alarm = model.createAlarm( "my alarm" );
        String prop = (String) alarm.getProperty( "my property" );
        assertEquals( "Property setting.", "my value", prop );
        model.addProperty( "my other property", "my other value" );
        prop = (String) alarm.getProperty( "my property" );
        assertEquals( "Property setting.", "my value", prop );
        prop = (String) alarm.getProperty( "my other property" );
        assertEquals( "Property setting.", "my other value", prop );

        model.removeProperty( "my property" );
        prop = (String) alarm.getProperty( "my property" );
        assertEquals( "Property setting.", null, prop );

        alarm.setProperty( "my other property", "this value" );
        prop = (String) alarm.getProperty( "my other property" );
        assertEquals( "Property setting.", "this value", prop );

        alarm.setProperty( "my other property", null );
        prop = (String) alarm.getProperty( "my other property" );
        assertEquals( "Property setting.", "my other value", prop );

        alarm.setProperty( "my other property", "this value" );
        model.removeProperty( "my other property" );
        prop = (String) alarm.getProperty( "my other property" );
        assertEquals( "Property setting.", "this value", prop );
    }

    public void testNewDefaultModel()
        throws Exception
    {
        List alarms = model.getAlarms();
        assertEquals( "Alarms registered.", 0, alarms.size() );
        Alarm alarm1 = model.createAlarm( "my alarm 1" );
        Alarm alarm2 = model.createAlarm( "my alarm 2" );
        Alarm alarm3 = model.createAlarm( "my alarm 3" );
        Alarm alarm4 = model.createAlarm( "my alarm 4" );
        Alarm alarm5 = model.createAlarm( "my alarm 5" );
        alarms = model.getAlarms();
        assertEquals( "Alarms registered.", 5, alarms.size() );
        AlarmModel model = new AlarmModelImpl( provider );
        this.model.newDefaultModelSet( model );
        alarms = this.model.getAlarms();
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
        assertEquals( "Wrong number of AlarmListener calls.", 5, counter );
    }

    public void alarmFired( AlarmEvent event )
    {
        counter++;
    }
}
