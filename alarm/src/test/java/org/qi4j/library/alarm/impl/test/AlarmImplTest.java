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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmModelProvider;
import org.qi4j.library.alarm.AlarmListener;
import org.qi4j.library.alarm.AlarmCreationException;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmTriggerException;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.providers.simple.SimpleModelProvider;
import org.qi4j.library.alarm.impl.AlarmModelImpl;
import org.qi4j.library.alarm.impl.AlarmImpl;

public class AlarmImplTest extends TestCase
    implements AlarmListener
{
    private Alarm underTest;
    private int fired;
    private AlarmModelImpl model;

    public void setUp()
        throws Exception
    {
        AlarmModelProvider spi = new SimpleModelProvider();
        model = new AlarmModelImpl( spi );
        underTest = model.createAlarm( "TestCase Alarm" );
    }

    public void testCreationProblems()
        throws Exception
    {
        try
        {
            new AlarmImpl( model, null );
            fail( "Alarm created with null name." );
        } catch( AlarmCreationException e )
        {
            // expected.
        }
        try
        {
            new AlarmImpl( model, "" );
            fail( "Alarm created with empty string name." );
        } catch( AlarmCreationException e )
        {
            // expected.
        }
        try
        {
            new AlarmImpl( model, "\n \n" );
            fail( "Alarm created with white space name." );
        } catch( AlarmCreationException e )
        {
            // expected.
        }
        try
        {
            new AlarmImpl( null, "abc" );
            fail( "Alarm created with null AlarmModel." );
        } catch( AlarmCreationException e )
        {
            // expected.
        }

    }

    public void testName() throws Exception
    {
        assertEquals( "TestCase Alarm", underTest.getName() );
    }

    public void testDescription() throws Exception
    {
//        assertEquals( "This is a default Locale description of a testcase Alarm.", underTest.getDescription() );

        Locale english = Locale.UK;
        assertEquals( "This is a UK Locale description of a testcase Alarm.", underTest.getDescription(english) );

        Locale swedish = new Locale( "sv" );
        assertEquals( "Detta \u00E5r en svensk beskrivning av ett testlarm.", underTest.getDescription( swedish ) );

    }

    public void testState()
    {
        assertEquals( "normal", underTest.getState().getName() );
        boolean condition = underTest.getCondition();
        assertEquals( false, condition );
    }

    public void testCheckAndSetModel()
        throws Exception
    {
        AlarmModel model = underTest.getAlarmModel();
        assertEquals( this.model, model );
        AlarmModelProvider spi = new SimpleModelProvider();
        underTest.setAlarmModel( new AlarmModelImpl( spi ) );
        model = underTest.getAlarmModel();
        assertFalse( "AlarmModel still considered equal.", this.model.equals( model ) );

    }

    public void testProperties()
    {
        String alarmText = (String) underTest.getProperty( "text" );
        assertNull( alarmText );

        underTest.setProperty( "text", "TestCase Alarm" );
        alarmText = (String) underTest.getProperty( "text" );
        assertEquals( "TestCase Alarm", alarmText );

        Map props1 = underTest.getProperties();
        Map props2 = new HashMap();
        props2.put( "text", "TestCase Alarm" );
        assertEquals( props2, props1 );

        underTest.setProperty( "text", null );
        Map props3 = underTest.getProperties();
        Map props4 = new HashMap();
        assertEquals( props4, props3 );
    }

    public void testInvalidTrigger()
        throws Exception
    {
        try
        {
            underTest.trigger( this, "my-special-trigger" );
            fail( "AlarmTriggerException was not thrown." );
        } catch( AlarmTriggerException e )
        {
            // Expected.
        }
    }

    public void testNoEvent()
        throws Exception
    {
        underTest.addAlarmListener( this );
        underTest.deactivate( this );
        assertEquals( 0, fired );
    }

    public void testListener()
    {
        underTest.removeAlarmListener( null );  // make sure it doesn't fail.
        underTest.removeAlarmListener( this );  // make sure it doesn't fail.

        underTest.addAlarmListener( this );
        underTest.activate( this );
        assertEquals( 1, fired );
        underTest.addAlarmListener( this );
        underTest.deactivate( this );
        assertEquals( 3, fired );
        underTest.removeAlarmListener( this );
        underTest.activate( this );
        assertEquals( 4, fired );
        underTest.removeAlarmListener( this );
        underTest.deactivate( this );
        assertEquals( 4, fired );
    }

    public void alarmFired( AlarmEvent event )
    {
        fired++;
    }
}
