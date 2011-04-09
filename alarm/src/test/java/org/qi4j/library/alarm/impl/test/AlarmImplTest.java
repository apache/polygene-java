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
    private Alarm m_TestAlarm;
    private int m_Fired;
    private AlarmModelImpl m_Model;

    public void setUp()
        throws Exception
    {
        AlarmModelProvider spi = new SimpleModelProvider();
        m_Model = new AlarmModelImpl( spi );
        m_TestAlarm = m_Model.createAlarm( "TestCase Alarm" );
    }

    public void testCreationProblems()
        throws Exception
    {
        try
        {
            new AlarmImpl( m_Model, null );
            fail( "Alarm created with null name." );
        } catch( AlarmCreationException e )
        {
            // expected.
        }
        try
        {
            new AlarmImpl( m_Model, "" );
            fail( "Alarm created with empty string name." );
        } catch( AlarmCreationException e )
        {
            // expected.
        }
        try
        {
            new AlarmImpl( m_Model, "\n \n" );
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
        assertEquals( "TestCase Alarm", m_TestAlarm.getName() );
    }

    public void testDescription() throws Exception
    {
//        assertEquals( "This is a default Locale description of a testcase Alarm.", m_TestAlarm.getDescription() );

        Locale english = Locale.UK;
        assertEquals( "This is a UK Locale description of a testcase Alarm.", m_TestAlarm.getDescription(english) );

        Locale swedish = new Locale( "sv" );
        assertEquals( "Detta \u00E5r en svensk beskrivning av ett testlarm.", m_TestAlarm.getDescription( swedish ) );

    }

    public void testState()
    {
        assertEquals( "normal", m_TestAlarm.getState().getName() );
        boolean condition = m_TestAlarm.getCondition();
        assertEquals( false, condition );
    }

    public void testCheckAndSetModel()
        throws Exception
    {
        AlarmModel model = m_TestAlarm.getAlarmModel();
        assertEquals( m_Model, model );
        AlarmModelProvider spi = new SimpleModelProvider();
        m_TestAlarm.setAlarmModel( new AlarmModelImpl( spi ) );
        model = m_TestAlarm.getAlarmModel();
        assertFalse( "AlarmModel still considered equal.", m_Model.equals( model ) );

    }

    public void testProperties()
    {
        String alarmText = (String) m_TestAlarm.getProperty( "text" );
        assertNull( alarmText );

        m_TestAlarm.setProperty( "text", "TestCase Alarm" );
        alarmText = (String) m_TestAlarm.getProperty( "text" );
        assertEquals( "TestCase Alarm", alarmText );

        Map props1 = m_TestAlarm.getProperties();
        Map props2 = new HashMap();
        props2.put( "text", "TestCase Alarm" );
        assertEquals( props2, props1 );

        m_TestAlarm.setProperty( "text", null );
        Map props3 = m_TestAlarm.getProperties();
        Map props4 = new HashMap();
        assertEquals( props4, props3 );
    }

    public void testInvalidTrigger()
        throws Exception
    {
        try
        {
            m_TestAlarm.trigger( this, "my-special-trigger" );
            fail( "AlarmTriggerException was not thrown." );
        } catch( AlarmTriggerException e )
        {
            // Expected.
        }
    }

    public void testNoEvent()
        throws Exception
    {
        m_TestAlarm.addAlarmListener( this );
        m_TestAlarm.deactivate( this );
        assertEquals( 0, m_Fired );
    }

    public void testListener()
    {
        m_TestAlarm.removeAlarmListener( null );  // make sure it doesn't fail.
        m_TestAlarm.removeAlarmListener( this );  // make sure it doesn't fail.

        m_TestAlarm.addAlarmListener( this );
        m_TestAlarm.activate( this );
        assertEquals( 1, m_Fired );
        m_TestAlarm.addAlarmListener( this );
        m_TestAlarm.deactivate( this );
        assertEquals( 3, m_Fired );
        m_TestAlarm.removeAlarmListener( this );
        m_TestAlarm.activate( this );
        assertEquals( 4, m_Fired );
        m_TestAlarm.removeAlarmListener( this );
        m_TestAlarm.deactivate( this );
        assertEquals( 4, m_Fired );
    }

    public void alarmFired( AlarmEvent event )
    {
        m_Fired++;
    }
}
