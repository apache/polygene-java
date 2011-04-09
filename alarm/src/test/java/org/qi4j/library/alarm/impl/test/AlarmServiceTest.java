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
package org.qi4j.library.alarm.impl.test;

import java.util.List;
import junit.framework.TestCase;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmCreationException;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmListener;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmModelProvider;
import org.qi4j.library.alarm.impl.AlarmModelImpl;
import org.qi4j.library.alarm.impl.AlarmServiceImpl;
import org.qi4j.library.alarm.providers.simple.SimpleModelProvider;

public class AlarmServiceTest extends TestCase
{

    private AlarmServiceImpl m_AlarmService;
    private AlarmModel m_Model;
    private AlarmModelProvider m_Provider;

    public void setUp()
    {
        m_AlarmService = new AlarmServiceImpl();
        m_Provider = new SimpleModelProvider();
        m_Model = new AlarmModelImpl( m_Provider );
    }

    public void testGetAlarmModels()
    {
        AlarmModel[] models = m_AlarmService.getAlarmModels();
        assertNotNull( models );
        assertEquals( 0, models.length );

        m_AlarmService.addAlarmModel( m_Model );
        models = m_AlarmService.getAlarmModels();
        assertEquals( 1, models.length );
        assertEquals( m_Model, models[ 0 ] );

        m_AlarmService.addAlarmModel( m_Model );
        models = m_AlarmService.getAlarmModels();
        assertEquals( 2, models.length );
        assertEquals( m_Model, models[ 0 ] );
        assertEquals( m_Model, models[ 1 ] );

        m_AlarmService.removeAlarmModel( m_Model );
        models = m_AlarmService.getAlarmModels();
        assertEquals( 1, models.length );
        assertEquals( m_Model, models[ 0 ] );

        m_AlarmService.removeAlarmModel( m_Model );
        models = m_AlarmService.getAlarmModels();
        assertEquals( 0, models.length );
    }

    public void testDefaultModel()
        throws Exception
    {
        AlarmModel[] models = m_AlarmService.getAlarmModels();
        assertNotNull( models );
        assertEquals( 0, models.length );

        AlarmModel model = m_AlarmService.getDefaultAlarmModel();
        assertNull( model );

        m_AlarmService.addAlarmModel( m_Model );
        model = m_AlarmService.getDefaultAlarmModel();
        assertEquals( m_Model, model );

        AlarmModel newModel = new AlarmModelImpl( m_Provider );
        m_AlarmService.addAlarmModel( newModel );
        model = m_AlarmService.getDefaultAlarmModel();
        assertEquals( m_Model, model );

        m_AlarmService.setDefaultAlarmModel( newModel );
        model = m_AlarmService.getDefaultAlarmModel();
        assertEquals( newModel, model );

        m_AlarmService.removeAlarmModel( newModel );
        model = m_AlarmService.getDefaultAlarmModel();
        assertEquals( m_Model, model );

        m_AlarmService.removeAlarmModel( m_Model );
        model = m_AlarmService.getDefaultAlarmModel();
        assertNull( model );
    }

    /**
     * @throws Exception testcase throw
     * @noinspection UnusedAssignment
     */
    public void testAlarmRegistration()
        throws Exception
    {
        AlarmModel newModel = new AlarmModelImpl( m_Provider );
        m_AlarmService.addAlarmModel( newModel );
        List alarms = m_AlarmService.getAlarms();
        assertEquals( "Registered alarms.", 0, alarms.size() );

        Alarm alarm = m_AlarmService.createAlarm( "TestAlarm" );
        alarms = m_AlarmService.getAlarms();
        assertEquals( "Registered alarms.", 1, alarms.size() );
        alarm.activate( this );

        alarms.clear();
        alarms = null;
        alarm = null; // drop the reference to it.
        for( int i = 0; i < 30; i++ )
        {
            System.gc();  // try to get it garbage collected.
            Thread.sleep( 100 );
            alarms = m_AlarmService.getAlarms();
            if( alarms.size() == 0 )
            {
                break;
            }
            alarms.clear();
            alarms = null;
        }
        alarms = m_AlarmService.getAlarms();
        assertEquals( "Dropped Alarm reference.", 0, alarms.size() );

        try
        {
            m_AlarmService.createAlarm( "\t" );
            fail( "illegal name not detected." );
        }
        catch( AlarmCreationException e )
        {
            // Expected.
        }
    }

    public void testListeners()
        throws Exception
    {
        AlarmModel newModel = new AlarmModelImpl( m_Provider );
        m_AlarmService.addAlarmModel( newModel );
        Alarm alarm = m_AlarmService.createAlarm( "TestAlarm" );

        CountingListener listener1 = new CountingListener();
        ExceptionThrowingListener listener2 = new ExceptionThrowingListener();
        ErrorThrowingListener listener3 = new ErrorThrowingListener();
        m_AlarmService.addAlarmListener( listener1 );
        m_AlarmService.addAlarmListener( listener3 );
        m_AlarmService.addAlarmListener( listener1 );
        try
        {
            alarm.activate( this );
            fail( "InternalError was expected to be thrown." );
        }
        catch( InternalError e )
        {
            // Expected. If an Error is thrown it should not be captured anywhere.
        }
        assertEquals( 1, listener1.getCounter() );      // One time, because the second listener would not be called.

        m_AlarmService.removeAlarmListener( listener3 );
        m_AlarmService.removeAlarmListener( listener1 );
        m_AlarmService.addAlarmListener( listener2 );
        m_AlarmService.addAlarmListener( listener1 );   // We should now have, 'counting', 'exception', 'counting' in the list.
        alarm.deactivate( this );   // No Exception should be thrown. The fireAlarm() should swallow it and ensure that
        // all listeners are called and then return.
        assertEquals( 3, listener1.getCounter() );

        List listeners = m_AlarmService.getAlarmListeners();
        assertEquals( "Listeners registered.", 3, listeners.size() );

        m_AlarmService.removeAlarmListener( listener1 );
        listeners = m_AlarmService.getAlarmListeners();
        assertEquals( "Listeners registered.", 2, listeners.size() );

        m_AlarmService.removeAlarmListener( listener1 );
        listeners = m_AlarmService.getAlarmListeners();
        assertEquals( "Listeners registered.", 1, listeners.size() );

        m_AlarmService.removeAlarmListener( listener1 );
        listeners = m_AlarmService.getAlarmListeners();
        assertEquals( "Listeners registered.", 1, listeners.size() );

        m_AlarmService.removeAlarmListener( listener2 );
        listeners = m_AlarmService.getAlarmListeners();
        System.out.println( listeners );
        assertEquals( "Listeners registered.", 0, listeners.size() );

        m_AlarmService.removeAlarmListener( listener2 );
        listeners = m_AlarmService.getAlarmListeners();
        assertEquals( "Listeners registered.", 0, listeners.size() );

        m_AlarmService.removeAlarmListener( null );
        listeners = m_AlarmService.getAlarmListeners();
        assertEquals( "Listeners registered.", 0, listeners.size() );
    }

    private class CountingListener
        implements AlarmListener
    {

        private int m_Counter;

        public CountingListener()
        {
            m_Counter = 0;
        }

        public void alarmFired( AlarmEvent event )
        {
            m_Counter = getCounter() + 1;
        }

        public int getCounter()
        {
            return m_Counter;
        }
    }

    private class ExceptionThrowingListener
        implements AlarmListener
    {

        public void alarmFired( AlarmEvent event )
            throws IllegalArgumentException
        {
            throw new IllegalArgumentException( "This is an intentional Exception, and it is not a sign of a problem." );
        }
    }

    private class ErrorThrowingListener
        implements AlarmListener
    {

        public void alarmFired( AlarmEvent event )
        {
            throw new InternalError( "This is an intentional java.lang.Error." );
        }
    }
}
