/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.library.alarm.providers.extended.tests;

import java.util.Locale;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmState;
import org.qi4j.library.alarm.AlarmTriggerException;
import org.qi4j.library.alarm.impl.AlarmModelImpl;
import org.qi4j.library.alarm.providers.extended.AcknowledgeEvent;
import org.qi4j.library.alarm.providers.extended.AcknowledgedState;
import org.qi4j.library.alarm.providers.extended.ActivatedState;
import org.qi4j.library.alarm.providers.extended.ActivationEvent;
import org.qi4j.library.alarm.providers.extended.DeactivatedState;
import org.qi4j.library.alarm.providers.extended.DeactivationEvent;
import org.qi4j.library.alarm.providers.extended.ExtendedModelProvider;
import org.qi4j.library.alarm.providers.extended.NormalState;
import org.qi4j.library.alarm.providers.extended.BlockEvent;
import org.qi4j.library.alarm.providers.extended.UnblockEvent;
import org.qi4j.library.alarm.providers.extended.DisableEvent;
import org.qi4j.library.alarm.providers.extended.EnableEvent;
import org.qi4j.library.alarm.providers.extended.DisabledState;
import org.qi4j.library.alarm.providers.extended.BlockedState;
import org.qi4j.library.alarm.providers.extended.ReactivatedState;

public class ExtendedModelSpiImplTest extends TestCase
{

    private Alarm underTest;
    private ExtendedModelProvider provider;
    private AlarmModel model;

    public void setUp()
        throws Exception
    {
        provider = new ExtendedModelProvider();
        model = new AlarmModelImpl( provider );
        underTest = model.createAlarm( "TestCase Alarm" );
    }

    public void testName()
        throws Exception
    {
        Assert.assertEquals( "org.qi4j.library.alarm.model.extended", provider.getName() );
    }

    public void testDescription()
        throws Exception
    {
        boolean test1 = provider.getDescription().toLowerCase().indexOf( "normal" ) >= 0;
        boolean test2 = provider.getDescription().toLowerCase().indexOf( "activated" ) >= 0;
        boolean test3 = provider.getDescription().toLowerCase().indexOf( "deactivated" ) >= 0;
        boolean test4 = provider.getDescription().toLowerCase().indexOf( "acknowledged" ) >= 0;
        boolean test5 = provider.getDescription().toLowerCase().indexOf( "activation" ) >= 0;
        boolean test6 = provider.getDescription().toLowerCase().indexOf( "deactivation" ) >= 0;
        boolean test7 = provider.getDescription().toLowerCase().indexOf( "acknowledge" ) >= 0;
        boolean test8 = provider.getDescription().toLowerCase().indexOf( "block" ) >= 0;
        boolean test9 = provider.getDescription().toLowerCase().indexOf( "unblock" ) >= 0;
        boolean test10 = provider.getDescription().toLowerCase().indexOf( "disable" ) >= 0;
        boolean test11 = provider.getDescription().toLowerCase().indexOf( "enable" ) >= 0;
        assertTrue( test1 && test2 && test3 && test4 && test5 && test6 && test7 && test8 && test9 && test10 && test11 );

        Locale english = new Locale( "en" );
        test1 = provider.getDescription( english ).toLowerCase().indexOf( "normal" ) >= 0;
        test2 = provider.getDescription( english ).toLowerCase().indexOf( "activated" ) >= 0;
        test3 = provider.getDescription( english ).toLowerCase().indexOf( "deactivated" ) >= 0;
        test4 = provider.getDescription( english ).toLowerCase().indexOf( "acknowledged" ) >= 0;
        test5 = provider.getDescription( english ).toLowerCase().indexOf( "activation" ) >= 0;
        test6 = provider.getDescription( english ).toLowerCase().indexOf( "deactivation" ) >= 0;
        test7 = provider.getDescription( english ).toLowerCase().indexOf( "acknowledge" ) >= 0;
        test8 = provider.getDescription( english ).toLowerCase().indexOf( "block" ) >= 0;
        test9 = provider.getDescription( english ).toLowerCase().indexOf( "unblock" ) >= 0;
        test10 = provider.getDescription( english ).toLowerCase().indexOf( "disable" ) >= 0;
        test11 = provider.getDescription( english ).toLowerCase().indexOf( "enable" ) >= 0;
        assertTrue( test1 && test2 && test3 && test4 && test5 && test6 && test7 && test8 && test9 && test10 && test11 );
    }

    public void testTriggers()
        throws Exception
    {
        String[] triggers = provider.getAlarmTriggers();
        assertEquals( 7, triggers.length );
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
            if( Alarm.TRIGGER_BLOCK.equals( triggers[ i ] ) )
            {
                result |= 8;
            }
            if( Alarm.TRIGGER_UNBLOCK.equals( triggers[ i ] ) )
            {
                result |= 16;
            }
            if( Alarm.TRIGGER_DISABLE.equals( triggers[ i ] ) )
            {
                result |= 32;
            }
            if( Alarm.TRIGGER_ENABLE.equals( triggers[ i ] ) )
            {
                result |= 64;
            }
        }
        assertEquals( 127, result );
        assertEquals( underTest.alarmState().getName(), "normal" );
    }

    public void testStateChangeFromNormal()
        throws Exception
    {
        Alarm alarm = model.createAlarm( "Another 1" );
        AlarmEvent event1 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertEquals( event1.getClass(), ActivationEvent.class );

        alarm = model.createAlarm( "Another 2" );
        AlarmEvent event2 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertNull( event2 );

        alarm = model.createAlarm( "Another 3" );
        AlarmEvent event3 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );

        alarm = model.createAlarm( "Another 4" );
        AlarmEvent event4 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_BLOCK );
        assertEquals( BlockEvent.class, event4.getClass() );

        alarm = model.createAlarm( "Another 5" );
        AlarmEvent event5 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = model.createAlarm( "Another 6" );
        AlarmEvent event6 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( DisableEvent.class, event6.getClass() );

        alarm = model.createAlarm( "Another 7" );
        AlarmEvent event7 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    public void testStateChangeFromActivated()
        throws Exception
    {
        Alarm alarm = model.createAlarm( "Another 1" );
        alarm.activate( this );

        AlarmEvent event1 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertNull( event1 );

        alarm = model.createAlarm( "Another 2" );
        alarm.activate( this );
        AlarmEvent event2 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertEquals( DeactivationEvent.class, event2.getClass() );

        alarm = model.createAlarm( "Another 3" );
        alarm.activate( this );
        AlarmEvent event3 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertEquals( AcknowledgeEvent.class, event3.getClass() );

        alarm = model.createAlarm( "Another 4" );
        alarm.activate( this );
        AlarmEvent event4 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_BLOCK);
        assertEquals( BlockEvent.class, event4.getClass() );

        alarm = model.createAlarm( "Another 5" );
        alarm.activate( this );
        AlarmEvent event5 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = model.createAlarm( "Another 6" );
        alarm.activate( this );
        AlarmEvent event6 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( DisableEvent.class, event6.getClass() );

        alarm = model.createAlarm( "Another 7" );
        alarm.activate( this );
        AlarmEvent event7 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    public void testStateChangeFromAcknowledged()
        throws Exception
    {
        Alarm alarm = model.createAlarm( "Another 1" );
        alarm.activate( this );
        alarm.acknowledge( this );

        AlarmEvent event1 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertNull( event1 );

        alarm = model.createAlarm( "Another 2" );
        alarm.activate( this );
        alarm.acknowledge( this );
        AlarmEvent event2 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertEquals( DeactivationEvent.class, event2.getClass() );

        alarm = model.createAlarm( "Another 3" );
        alarm.activate( this );
        alarm.acknowledge( this );
        AlarmEvent event3 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );

        alarm = model.createAlarm( "Another 4" );
        alarm.activate( this );
        alarm.acknowledge( this );
        AlarmEvent event4 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_BLOCK);
        assertEquals( BlockEvent.class, event4.getClass() );

        alarm = model.createAlarm( "Another 5" );
        alarm.activate( this );
        alarm.acknowledge( this );
        AlarmEvent event5 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = model.createAlarm( "Another 6" );
        alarm.activate( this );
        alarm.acknowledge( this );
        AlarmEvent event6 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( DisableEvent.class, event6.getClass() );

        alarm = model.createAlarm( "Another 7" );
        alarm.activate( this );
        alarm.acknowledge( this );
        AlarmEvent event7 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    public void testStateChangeFromDeactivated()
        throws Exception
    {
        Alarm alarm = model.createAlarm( "Another 1" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event1 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertEquals( ActivationEvent.class, event1.getClass() );

        alarm = model.createAlarm( "Another 2" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event2 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertNull( event2 );

        alarm = model.createAlarm( "Another 3" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event3 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertEquals( AcknowledgeEvent.class, event3.getClass() );

        alarm = model.createAlarm( "Another 4" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event4 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_BLOCK);
        assertEquals( BlockEvent.class, event4.getClass() );

        alarm = model.createAlarm( "Another 5" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event5 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = model.createAlarm( "Another 6" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event6 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( DisableEvent.class, event6.getClass() );

        alarm = model.createAlarm( "Another 7" );
        alarm.activate( this );
        alarm.deactivate( this );
        AlarmEvent event7 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    public void testStateChangeFromBlocked()
        throws Exception
    {
        Alarm alarm = model.createAlarm( "Another 1" );
        alarm.activate( this );
        alarm.trigger( this, "block" );
        AlarmEvent event1 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertNull( event1 );

        alarm = model.createAlarm( "Another 2" );
        alarm.activate( this );
        alarm.trigger( this, "block" );
        AlarmEvent event2 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertNull( event2 );

        alarm = model.createAlarm( "Another 3" );
        alarm.activate( this );
        alarm.trigger( this, "block" );
        AlarmEvent event3 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );

        alarm = model.createAlarm( "Another 4" );
        alarm.activate( this );
        alarm.trigger( this, "block" );
        AlarmEvent event4 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_BLOCK);
        assertNull( event4 );

        alarm = model.createAlarm( "Another 5" );
        alarm.activate( this );
        alarm.trigger( this, "block" );
        AlarmEvent event5 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_UNBLOCK );
        assertEquals( UnblockEvent.class, event5.getClass() );

        alarm = model.createAlarm( "Another 6" );
        alarm.activate( this );
        alarm.trigger( this, "block" );
        AlarmEvent event6 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( DisableEvent.class, event6.getClass() );

        alarm = model.createAlarm( "Another 7" );
        alarm.activate( this );
        alarm.trigger( this, "block" );
        AlarmEvent event7 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    public void testStateChangeFromDisabled()
        throws Exception
    {
        Alarm alarm = model.createAlarm( "Another 1" );
        alarm.activate( this );
        alarm.trigger( this, "disable" );
        AlarmEvent event1 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACTIVATION );
        assertNull( event1 );

        alarm = model.createAlarm( "Another 2" );
        alarm.activate( this );
        alarm.trigger( this, "disable" );
        AlarmEvent event2 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DEACTIVATION );
        assertNull( event2 );

        alarm = model.createAlarm( "Another 3" );
        alarm.activate( this );
        alarm.trigger( this, "disable" );
        AlarmEvent event3 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );

        alarm = model.createAlarm( "Another 4" );
        alarm.activate( this );
        alarm.trigger( this, "disable" );
        AlarmEvent event4 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_BLOCK);
        assertNull( event4 );

        alarm = model.createAlarm( "Another 5" );
        alarm.activate( this );
        alarm.trigger( this, "disable" );
        AlarmEvent event5 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = model.createAlarm( "Another 6" );
        alarm.activate( this );
        alarm.trigger( this, "disable" );
        AlarmEvent event6 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_DISABLE );
        assertNull( event6 );

        alarm = model.createAlarm( "Another 7" );
        alarm.activate( this );
        alarm.trigger( this, "disable" );
        AlarmEvent event7 = provider.executeStateChange( this, alarm, Alarm.TRIGGER_ENABLE );
        assertEquals( EnableEvent.class, event7.getClass() );
    }

    public void testIllegalTrigger()
        throws Exception
    {
        try
        {
            provider.executeStateChange( this, underTest, "my-trigger" );
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
        underTest.activate( this );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new NormalState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new ActivatedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );

        assertEquals( this, event.triggeredBy() );
    }

    public void testActivatedToDeactivated()
        throws Exception
    {
        underTest.activate( this );
        underTest.deactivate( this );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new DeactivatedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );

        assertEquals( this, event.triggeredBy() );
    }

    public void testActivatedToAcknowledged()
        throws Exception
    {
        underTest.activate( this );
        underTest.acknowledge( this );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new AcknowledgedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );

        assertEquals( this, event.triggeredBy() );
    }

    public void testDeactivatedToNormal()
        throws Exception
    {
        underTest.activate( this );
        underTest.deactivate( this );
        underTest.acknowledge( this );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new DeactivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new NormalState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );

        assertEquals( this, event.triggeredBy() );
    }

    public void testAcknowledgedToNormal()
        throws Exception
    {
        underTest.activate( this );
        underTest.acknowledge( this );
        underTest.deactivate( this );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new AcknowledgedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new NormalState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );

        assertEquals( this, event.triggeredBy() );
    }

    public void testDisabledToNormal()
        throws Exception
    {
        underTest.activate( this );
        underTest.trigger( this, "disable" );
        underTest.trigger( this, "enable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new DisabledState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new NormalState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testBlockedToNormal()
        throws Exception
    {
        underTest.activate( this );
        underTest.trigger( this, "block" );
        underTest.trigger( this, "unblock" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new BlockedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new NormalState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testNormalToBlocked()
        throws Exception
    {
        underTest.trigger( this, "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new NormalState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new BlockedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testActivatedToBlocked()
        throws Exception
    {
        underTest.activate( this );
        underTest.trigger( this, "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new BlockedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testDeactivatedToBlocked()
        throws Exception
    {
        underTest.activate( this );
        underTest.deactivate( this );
        underTest.trigger( this, "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new DeactivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new BlockedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testAcknowledgedToBlocked()
        throws Exception
    {
        underTest.activate( this );
        underTest.acknowledge( this );
        underTest.trigger( this, "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new AcknowledgedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new BlockedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testNormalToDisabled()
        throws Exception
    {
        underTest.trigger( this, "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new NormalState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new DisabledState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testActivatedToDisabled()
        throws Exception
    {
        underTest.activate( this );
        underTest.trigger( this, "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new DisabledState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testDeactivatedToDisabled()
        throws Exception
    {
        underTest.activate( this );
        underTest.deactivate( this );
        underTest.trigger( this, "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new DeactivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new DisabledState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testAcknowledgedToDisabled()
        throws Exception
    {
        underTest.activate( this );
        underTest.acknowledge( this );
        underTest.trigger( this, "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new AcknowledgedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new DisabledState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testBlockedToDisabled()
        throws Exception
    {
        underTest.activate( this );
        underTest.trigger( this, "block" );
        underTest.trigger( this, "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new BlockedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new DisabledState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testDisabledToBlocked()
        throws Exception
    {
        underTest.activate( this );
        underTest.trigger( this, "disable" );
        underTest.trigger( this, "block" );       // This trigger should be ignored.
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new DisabledState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );
        assertEquals( this, event.triggeredBy() );
    }

    public void testConditionChanges1()
        throws Exception
    {
        underTest.updateCondition( false );
        AlarmEvent event = underTest.history().lastEvent();
        assertNull( "Generated an event but should have not.", event );
    }

    public void testConditionChanges2()
        throws Exception
    {
        underTest.updateCondition( true );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new NormalState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new ActivatedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );

        assertEquals( underTest, event.triggeredBy() );
    }

    public void testConditionChanges3()
        throws Exception
    {
        underTest.updateCondition( true );
        underTest.updateCondition( false );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmState oldstate = event.oldState();
        Assert.assertEquals( new ActivatedState().getName(), oldstate.getName() );

        AlarmState newstate = event.newState();
        Assert.assertEquals( new DeactivatedState().getName(), newstate.getName() );

        Alarm eventalarm = event.alarm();
        assertEquals( underTest, eventalarm );

        assertEquals( underTest, event.triggeredBy() );
    }

    public void testComputeCondition()
        throws Exception
    {
        AlarmState s1 = new NormalState();
        assertFalse( provider.computeCondition( s1 ) );
        AlarmState s2 = new ActivatedState();
        assertTrue( provider.computeCondition( s2 ) );
        AlarmState s3 = new DeactivatedState();
        assertFalse( provider.computeCondition( s3 ) );
        AlarmState s4 = new AcknowledgedState();
        assertTrue( provider.computeCondition( s4 ) );

        AlarmState s5 = new DisabledState();
        assertFalse( provider.computeCondition( s5 ) );
        AlarmState s6 = new BlockedState();
        assertFalse( provider.computeCondition( s6 ) );
        AlarmState s7 = new ReactivatedState();
        assertTrue( provider.computeCondition( s7 ) );
    }

    public void testComputeTriggerNormal()
        throws Exception
    {
        AlarmState state = new NormalState();
        String trigger1 = provider.computeTrigger( state, true );
        String trigger2 = provider.computeTrigger( state, false );
        assertEquals( Alarm.TRIGGER_ACTIVATION, trigger1 );
        assertEquals( null, trigger2 );
    }

    public void testComputeTriggerActivated()
    {
        AlarmState state = new ActivatedState();
        String trigger1 = provider.computeTrigger( state, true );
        String trigger2 = provider.computeTrigger( state, false );
        assertEquals( null, trigger1 );
        assertEquals( Alarm.TRIGGER_DEACTIVATION, trigger2 );
    }

    public void testComputeTRiggerDeactivated()
    {
        AlarmState state = new DeactivatedState();
        String trigger1 = provider.computeTrigger( state, true );
        String trigger2 = provider.computeTrigger( state, false );
        assertEquals( Alarm.TRIGGER_ACTIVATION, trigger1 );
        assertEquals( null, trigger2 );
    }

    public void testComputeTriggerAcknowledged()
    {
        AlarmState state = new AcknowledgedState();
        String trigger1 = provider.computeTrigger( state, true );
        String trigger2 = provider.computeTrigger( state, false );
        assertEquals( null, trigger1 );
        assertEquals( Alarm.TRIGGER_DEACTIVATION, trigger2 );
    }

    public void testComputeTriggerReactivated()
    {
        AlarmState state = new ReactivatedState();
        String trigger1 = provider.computeTrigger( state, true );
        String trigger2 = provider.computeTrigger( state, false );
        assertEquals( null, trigger1 );
        assertEquals( Alarm.TRIGGER_DEACTIVATION, trigger2 );
    }

    public void testComputeTriggerBlocked()
    {
        AlarmState state = new BlockedState();
        String trigger1 = provider.computeTrigger( state, true );
        String trigger2 = provider.computeTrigger( state, false );
        assertEquals( null, trigger1 );
        assertEquals( null, trigger2 );
    }

    public void testComputeTriggerDisabled()
    {
        AlarmState state = new DisabledState();
        String trigger1 = provider.computeTrigger( state, true );
        String trigger2 = provider.computeTrigger( state, false );
        assertEquals( null, trigger1 );
        assertEquals( null, trigger2 );
    }
}
