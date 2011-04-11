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
import junit.framework.Assert;

import java.util.Map;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmModelProvider;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmListener;
import org.qi4j.library.alarm.AlarmHistory;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.providers.simple.SimpleModelProvider;
import org.qi4j.library.alarm.impl.AlarmModelImpl;

public class AlarmHistoryImplTest extends TestCase
    implements AlarmListener
{
    private int eventCounter = 0;

    private Alarm underTest;

    public void setUp()
        throws Exception
    {
        AlarmModelProvider spi = new SimpleModelProvider();
        AlarmModel model = new AlarmModelImpl( spi );
        underTest = model.createAlarm( "TestCase Alarm" );
    }

    public void testEmpty()
        throws Exception
    {
        AlarmHistory hist = underTest.history();
        AlarmEvent event1 = hist.firstEvent();
        AlarmEvent event2 = hist.lastEvent();
        assertNull( event1 );
        assertNull( event2 );
        assertEquals( "Activate Counter", 0, hist.activateCounter() );
    }

    public void testFirstNotLast()
        throws Exception
    {
        underTest.updateCondition( true );
        underTest.updateCondition( false );
        AlarmHistory hist = underTest.history();
        AlarmEvent event1 = hist.firstEvent();
        AlarmEvent event2 = hist.lastEvent();
        assertFalse( event1.equals( event2 ) );
        Assert.assertEquals( "activated", event1.newState().getName() );
        Assert.assertEquals( "normal", event2.newState().getName() );
    }

    public void testGetPosition()
        throws Exception
    {
        underTest.addAlarmListener( this );
        AlarmHistory hist = underTest.history();
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );

        assertEquals( 5, eventCounter );
        assertEquals( 5, hist.getAllAlarmEvents().size() );

        AlarmEvent event = hist.eventAt( -1 );
        assertNull( event );
        event = hist.eventAt( 5 );
        assertNull( event );
        event = hist.eventAt( 0 );
        assertEquals( "activation", event.nameInDefaultLocale() );
        event = hist.eventAt( 1 );
        assertEquals( "deactivation", event.nameInDefaultLocale() );
        event = hist.eventAt( 2 );
        assertEquals( "activation", event.nameInDefaultLocale() );
        event = hist.eventAt( 3 );
        assertEquals( "deactivation", event.nameInDefaultLocale() );
        event = hist.eventAt( 4 );
        assertEquals( "activation", event.nameInDefaultLocale() );


    }

    public void testGetPositionFromLast()
        throws Exception
    {
        underTest.addAlarmListener( this );
        AlarmHistory hist = underTest.history();
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );

        assertEquals( 5, eventCounter );
        assertEquals( 5, hist.getAllAlarmEvents().size() );

        AlarmEvent event = hist.eventAtEnd( -1 );
        assertNull( event );
        event = hist.eventAtEnd( 5 );
        assertNull( event );
        event = hist.eventAtEnd( 4 );
        assertEquals( "activation", event.nameInDefaultLocale() );
        event = hist.eventAtEnd( 3 );
        assertEquals( "deactivation", event.nameInDefaultLocale() );
        event = hist.eventAtEnd( 2 );
        assertEquals( "activation", event.nameInDefaultLocale() );
        event = hist.eventAtEnd( 1 );
        assertEquals( "deactivation", event.nameInDefaultLocale() );
        event = hist.eventAtEnd( 0 );
        assertEquals( "activation", event.nameInDefaultLocale() );
    }

    public void testCounters()
        throws Exception
    {
        AlarmHistory hist = underTest.history();
        Map counters = hist.counters();

        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        verifyCounters( counters, 1, 0 );

        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        verifyCounters( counters, 1, 1 );

        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        verifyCounters( counters, 2, 1 );

        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        verifyCounters( counters, 2, 2 );

        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        verifyCounters( counters, 2, 2 );

        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        verifyCounters( counters, 3, 2 );

        int activateCounters = hist.activateCounter();
        assertEquals( 3, activateCounters );

        hist.resetActivateCounter();
        verifyCounters( counters, 0, 2 );

        hist.resetAllCounters();
        verifyCounters( counters, 0, 0 );

    }

    private void verifyCounters( Map counters, int c1, int c2 )
    {
        Number n1 = (Number) counters.get( Alarm.TRIGGER_ACTIVATION );
        Number n2 = (Number) counters.get( Alarm.TRIGGER_DEACTIVATION );
        if( n1 == null )
            assertEquals( 0, c1 );
        else
            assertEquals( new Integer(c1), n1 );

        if( n2 == null )
            assertEquals( 0, c2 );
        else
            assertEquals( new Integer(c2), n2 );
    }

    public void testSetMaxSize()
        throws Exception
    {
        underTest.addAlarmListener( this );
        AlarmHistory hist = underTest.history();
        assertEquals( 0, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        assertEquals( 1, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        assertEquals( 2, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        assertEquals( 3, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        assertEquals( 4, hist.getAllAlarmEvents().size() );

        int maxsize = hist.maxSize();
        assertEquals( 30, maxsize );

        hist.setMaxSize(3);
        assertEquals( 3, hist.getAllAlarmEvents().size() );

        hist.setMaxSize(0);
        assertEquals( 0, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        assertEquals( 0, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        assertEquals( 0, hist.getAllAlarmEvents().size() );
        hist.setMaxSize(2);
        assertEquals( 0, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        assertEquals( 1, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_DEACTIVATION );
        assertEquals( 2, hist.getAllAlarmEvents().size() );
        underTest.trigger( this, Alarm.TRIGGER_ACTIVATION );
        assertEquals( 2, hist.getAllAlarmEvents().size() );
        assertEquals( 9, eventCounter );
    }

    public void testToString()
        throws Exception
    {
        AlarmHistory hist = underTest.history();
        String str = hist.toString();
        assertEquals( "history[maxsize=30, size=0]", str );
        underTest.activate( this );
        str = hist.toString();
        assertEquals( "history[maxsize=30, size=1, " + Alarm.TRIGGER_ACTIVATION + "=1]", str );
        underTest.deactivate( this );
        str = hist.toString();
        assertEquals( "history[maxsize=30, size=2, " + Alarm.TRIGGER_DEACTIVATION + "=1, " + Alarm.TRIGGER_ACTIVATION + "=1]", str );

    }

    public void alarmFired( AlarmEvent event )
    {
        eventCounter++;
    }
}
