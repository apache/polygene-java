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

import java.util.Locale;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmState;
import org.qi4j.library.alarm.AlarmTriggerException;
import org.qi4j.library.alarm.impl.AlarmModelImpl;
import org.qi4j.library.alarm.providers.standard.AcknowledgeEvent;
import org.qi4j.library.alarm.providers.standard.AcknowledgedState;
import org.qi4j.library.alarm.providers.standard.ActivatedState;
import org.qi4j.library.alarm.providers.standard.ActivationEvent;
import org.qi4j.library.alarm.providers.standard.DeactivatedState;
import org.qi4j.library.alarm.providers.standard.DeactivationEvent;
import org.qi4j.library.alarm.providers.standard.NormalState;
import org.qi4j.library.alarm.providers.standard.StandardModelProvider;

public class StandardModelSpiImplTest extends TestCase
{
    private Alarm m_Alarm;
    private StandardModelProvider m_Provider;
    private AlarmModel m_Model;

    public void setUp()
        throws Exception
    {
        m_Provider = new StandardModelProvider();
        m_Model = new AlarmModelImpl( m_Provider );
        m_Alarm = m_Model.createAlarm( "TestCase Alarm" );
    }

    public void testName()
        throws Exception
    {
        StandardModelProvider spi = new StandardModelProvider();
        assertEquals( "org.qi4j.library.alarm.model.standard", spi.getName() );
    }

    public void testDescription()
        throws Exception
    {
        StandardModelProvider spi = new StandardModelProvider();
        boolean test1 = spi.getDescription().toLowerCase().indexOf( "normal" ) >= 0;
        boolean test2 = spi.getDescription().toLowerCase().indexOf( "activated" ) >= 0;
        boolean test3 = spi.getDescription().toLowerCase().indexOf( "deactivated" ) >= 0;
        boolean test4 = spi.getDescription().toLowerCase().indexOf( "acknowledged" ) >= 0;
        boolean test5 = spi.getDescription().toLowerCase().indexOf( "activation" ) >= 0;
        boolean test6 = spi.getDescription().toLowerCase().indexOf( "deactivation" ) >= 0;
        boolean test7 = spi.getDescription().toLowerCase().indexOf( "acknowledge" ) >= 0;
        assertTrue( test1 && test2 && test3 && test4 && test5 && test6 && test7 );

        Locale english = new Locale( "en" );
        test1 = spi.getDescription( english ).toLowerCase().indexOf( "normal" ) >= 0;
        test2 = spi.getDescription( english ).toLowerCase().indexOf( "activated" ) >= 0;
        test3 = spi.getDescription( english ).toLowerCase().indexOf( "deactivated" ) >= 0;
        test4 = spi.getDescription( english ).toLowerCase().indexOf( "acknowledged" ) >= 0;
        test5 = spi.getDescription( english ).toLowerCase().indexOf( "activation" ) >= 0;
        test6 = spi.getDescription( english ).toLowerCase().indexOf( "deactivation" ) >= 0;
        test7 = spi.getDescription( english ).toLowerCase().indexOf( "acknowledge" ) >= 0;
        assertTrue( test1 && test2 && test3 && test4 && test5 && test6 && test7 );
    }

    public void testTriggers()
        throws Exception
    {
        String[] triggers = m_Provider.getAlarmTriggers();
        assertEquals( 3, triggers.length );
        int result = 0;
        for( int i = 0; i < triggers.length; i++ )
        {
            if( Alarm.TRIGGER_ACTIVATION.equals( triggers[ i ] ) )
            {
                result |= 1;
            }
            if( Alarm.TRIGGER_DEACTIVATION.equals( triggers[ i ] ) )
            {
                result |= 2;
            }
            if( Alarm.TRIGGER_ACKNOWLEDGE.equals( triggers[ i ] ) )
            {
                result |= 4;
            }
        }
        assertEquals( 7, result );
        assertEquals( m_Alarm.getState().getName(), "normal" );
    }

    public void testStateChangeFromNormal()
        throws Exception
    {
        Alarm alarm = m_Model.createAlarm( "Another 1" );
        AlarmEvent event1 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertEquals( event1.getClass(), ActivationEvent.class );

        alarm = m_Model.createAlarm( "Another 2" );
        AlarmEvent event2 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertNull( event2 );

        alarm = m_Model.createAlarm( "Another 3" );
        AlarmEvent event3 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );
    }

    public void testStateChangeFromActivated()
        throws Exception
    {
        Alarm alarm = m_Model.createAlarm( "Another 1" );
        alarm.activate( this );

        AlarmEvent event1 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertNull( event1 );

        alarm = m_Model.createAlarm( "Another 2" );
        alarm.activate( this );
        AlarmEvent event2 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertEquals( DeactivationEvent.class, event2.getClass() );

        alarm = m_Model.createAlarm( "Another 3" );
        alarm.activate( this );
        AlarmEvent event3 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertEquals( AcknowledgeEvent.class, event3.getClass() );
    }

    public void testStateChangeFromAcknowledged()
        throws Exception
    {
        Alarm alarm = m_Model.createAlarm( "Another 1" );
        alarm.activate( this );
        alarm.acknowledge( this );

        AlarmEvent event1 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertNull( event1 );

        alarm = m_Model.createAlarm( "Another 2" );
        alarm.activate( this );
        alarm.acknowledge( this );
        AlarmEvent event2 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertEquals( DeactivationEvent.class, event2.getClass() );

        alarm = m_Model.createAlarm( "Another 3" );
        alarm.activate( this );
        alarm.acknowledge( this );
        AlarmEvent event3 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );
    }

    public void testStateChangeFromDeactivated()
        throws Exception
    {
        Alarm alarm = m_Model.createAlarm( "Another 1" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event1 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertEquals( ActivationEvent.class, event1.getClass() );

        alarm = m_Model.createAlarm( "Another 2" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event2 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertNull( event2 );

        alarm = m_Model.createAlarm( "Another 3" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event3 = m_Provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertEquals( AcknowledgeEvent.class, event3.getClass() );
    }

    public void testIllegalTrigger()
        throws Exception
    {
        try
        {
            m_Provider.executeStateChange( this, m_Alarm, "my-trigger" );
            fail( "AlarmTriggerException not thrown." );
        }
        catch( AlarmTriggerException e )
        {
            // Expected.
        }
    }

    public void testNormalToActivated()
        throws Exception
    {
        m_Alarm.activate( this );
        AlarmEvent event = m_Alarm.getHistory().getLast();

        AlarmState oldstate = event.getOldState();
        Assert.assertEquals( new NormalState().getName(), oldstate.getName() );

        AlarmState newstate = event.getNewState();
        Assert.assertEquals( new ActivatedState().getName(), newstate.getName() );

        Alarm eventalarm = event.getAlarm();
        assertEquals( m_Alarm, eventalarm );

        assertEquals( this, event.getTriggeredBy() );
    }

    public void testActivatedToDeactivated()
        throws Exception
    {
        m_Alarm.activate( this );
        m_Alarm.deactivate( this );
        AlarmEvent event = m_Alarm.getHistory().getLast();

        AlarmState oldstate = event.getOldState();
        assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.getNewState();
        Assert.assertEquals( new DeactivatedState().getName(), newstate.getName() );

        Alarm eventalarm = event.getAlarm();
        assertEquals( m_Alarm, eventalarm );

        assertEquals( this, event.getTriggeredBy() );
    }

    public void testActivatedToAcknowledged()
        throws Exception
    {
        m_Alarm.activate( this );
        m_Alarm.acknowledge( this );
        AlarmEvent event = m_Alarm.getHistory().getLast();

        AlarmState oldstate = event.getOldState();
        assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.getNewState();
        Assert.assertEquals( new AcknowledgedState().getName(), newstate.getName() );

        Alarm eventalarm = event.getAlarm();
        assertEquals( m_Alarm, eventalarm );

        assertEquals( this, event.getTriggeredBy() );
    }

    public void testDeactivatedToNormal()
        throws Exception
    {
        m_Alarm.activate( this );
        m_Alarm.deactivate( this );
        m_Alarm.acknowledge( this );
        AlarmEvent event = m_Alarm.getHistory().getLast();

        AlarmState oldstate = event.getOldState();
        assertEquals( new DeactivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.getNewState();
        assertEquals( new NormalState().getName(), newstate.getName() );

        Alarm eventalarm = event.getAlarm();
        assertEquals( m_Alarm, eventalarm );

        assertEquals( this, event.getTriggeredBy() );
    }

    public void testAcknowledgedToNormal()
        throws Exception
    {
        m_Alarm.activate( this );
        m_Alarm.acknowledge( this );
        m_Alarm.deactivate( this );
        AlarmEvent event = m_Alarm.getHistory().getLast();

        AlarmState oldstate = event.getOldState();
        assertEquals( new AcknowledgedState().getName(), oldstate.getName() );

        AlarmState newstate = event.getNewState();
        assertEquals( new NormalState().getName(), newstate.getName() );

        Alarm eventalarm = event.getAlarm();
        assertEquals( m_Alarm, eventalarm );

        assertEquals( this, event.getTriggeredBy() );
    }

    public void testConditionChanges1()
        throws Exception
    {
        m_Alarm.setCondition( false );
        AlarmEvent event = m_Alarm.getHistory().getLast();
        assertNull( "Generated an event but should have not.", event );
    }

    public void testConditionChanges2()
        throws Exception
    {
        m_Alarm.setCondition( true );
        AlarmEvent event = m_Alarm.getHistory().getLast();

        AlarmState oldstate = event.getOldState();
        assertEquals( new NormalState().getName(), oldstate.getName() );

        AlarmState newstate = event.getNewState();
        assertEquals( new ActivatedState().getName(), newstate.getName() );

        Alarm eventalarm = event.getAlarm();
        assertEquals( m_Alarm, eventalarm );

        assertEquals( m_Alarm, event.getTriggeredBy() );
    }

    public void testConditionChanges3()
        throws Exception
    {
        m_Alarm.setCondition( true );
        m_Alarm.setCondition( false );
        AlarmEvent event = m_Alarm.getHistory().getLast();

        AlarmState oldstate = event.getOldState();
        assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.getNewState();
        assertEquals( new DeactivatedState().getName(), newstate.getName() );

        Alarm eventalarm = event.getAlarm();
        assertEquals( m_Alarm, eventalarm );

        assertEquals( m_Alarm, event.getTriggeredBy() );
    }

    public void testComputeCondition()
        throws Exception
    {
        AlarmState s1 = new NormalState();
        assertFalse( m_Provider.computeCondition( s1 ) );
        AlarmState s2 = new ActivatedState();
        assertTrue( m_Provider.computeCondition( s2 ) );
        AlarmState s3 = new DeactivatedState();
        assertFalse( m_Provider.computeCondition( s3 ) );
        AlarmState s4 = new AcknowledgedState();
        assertTrue( m_Provider.computeCondition( s4 ) );
    }

    public void testComputeTrigger()
        throws Exception
    {
        AlarmState s1 = new NormalState();
        AlarmState s2 = new ActivatedState();
        AlarmState s3 = new DeactivatedState();
        AlarmState s4 = new AcknowledgedState();
        String trigger1 = m_Provider.computeTrigger( s1, true );
        String trigger2 = m_Provider.computeTrigger( s2, true );
        String trigger3 = m_Provider.computeTrigger( s3, true );
        String trigger4 = m_Provider.computeTrigger( s4, true );
        String trigger5 = m_Provider.computeTrigger( s1, false );
        String trigger6 = m_Provider.computeTrigger( s2, false );
        String trigger7 = m_Provider.computeTrigger( s3, false );
        String trigger8 = m_Provider.computeTrigger( s4, false );
        assertEquals( Alarm.TRIGGER_ACTIVATION, trigger1 );
        assertEquals( null, trigger2 );
        assertEquals( Alarm.TRIGGER_ACTIVATION, trigger3 );
        assertEquals( null, trigger4 );
        assertEquals( null, trigger5 );
        assertEquals( Alarm.TRIGGER_DEACTIVATION, trigger6 );
        assertEquals( null, trigger7 );
        assertEquals( Alarm.TRIGGER_DEACTIVATION, trigger8 );
    }
}
