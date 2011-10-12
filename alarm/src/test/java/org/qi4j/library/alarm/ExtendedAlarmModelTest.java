/*
 * Copyright 2006-2011 Niclas Hedhman.
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
package org.qi4j.library.alarm;

import junit.framework.Assert;
import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

public class ExtendedAlarmModelTest
    extends AbstractQi4jTest
{

    @SuppressWarnings( { "unchecked" } )
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( TestAlarmModel.class );
        module.services( AlarmSystemService.class );
        module.services( MemoryEntityStoreService.class );
        module.services( UuidIdentityGeneratorService.class );
        module.entities( AlarmEntity.class );
        module.forMixin( AlarmHistory.class ).declareDefaults().maxSize().set( 10 );
        module.values( AlarmEvent.class );
        module.values( AlarmCategory.class );
        module.values( AlarmStatus.class );
    }

    @Mixins( ExtendedAlarmModelService.ExtendedAlarmModelMixin.class )
    public interface TestAlarmModel
        extends AlarmModel, ServiceComposite
    {
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        module.newUnitOfWork();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        UnitOfWork uow = module.currentUnitOfWork();
        if( uow != null )
        {
            uow.discard();
        }
        super.tearDown();
    }

    @Test
    public void testDescription()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        boolean test1 = provider.modelDescription().toLowerCase().indexOf( "normal" ) >= 0;
        boolean test2 = provider.modelDescription().toLowerCase().indexOf( "activated" ) >= 0;
        boolean test3 = provider.modelDescription().toLowerCase().indexOf( "deactivated" ) >= 0;
        boolean test4 = provider.modelDescription().toLowerCase().indexOf( "acknowledged" ) >= 0;
        boolean test5 = provider.modelDescription().toLowerCase().indexOf( "activation" ) >= 0;
        boolean test6 = provider.modelDescription().toLowerCase().indexOf( "deactivation" ) >= 0;
        boolean test7 = provider.modelDescription().toLowerCase().indexOf( "acknowledge" ) >= 0;
        boolean test8 = provider.modelDescription().toLowerCase().indexOf( "block" ) >= 0;
        boolean test9 = provider.modelDescription().toLowerCase().indexOf( "unblock" ) >= 0;
        boolean test10 = provider.modelDescription().toLowerCase().indexOf( "disable" ) >= 0;
        boolean test11 = provider.modelDescription().toLowerCase().indexOf( "enable" ) >= 0;
        assertTrue( test1 && test2 && test3 && test4 && test5 && test6 && test7 && test8 && test9 && test10 && test11 );

        Locale english = new Locale( "en" );
        test1 = provider.modelDescription( english ).toLowerCase().contains( "normal" );
        test2 = provider.modelDescription( english ).toLowerCase().contains( "activated" );
        test3 = provider.modelDescription( english ).toLowerCase().contains( "deactivated" );
        test4 = provider.modelDescription( english ).toLowerCase().contains( "acknowledged" );
        test5 = provider.modelDescription( english ).toLowerCase().contains( "activation" );
        test6 = provider.modelDescription( english ).toLowerCase().contains( "deactivation" );
        test7 = provider.modelDescription( english ).toLowerCase().contains( "acknowledge" );
        test8 = provider.modelDescription( english ).toLowerCase().contains( "block" );
        test9 = provider.modelDescription( english ).toLowerCase().contains( "unblock" );
        test10 = provider.modelDescription( english ).toLowerCase().contains( "disable" );
        test11 = provider.modelDescription( english ).toLowerCase().contains( "enable" );
        assertTrue( test1 && test2 && test3 && test4 && test5 && test6 && test7 && test8 && test9 && test10 && test11 );
    }

    @Test
    public void testTriggers()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        Alarm underTest = createAlarm( "Test Alarm" );
        List<String> triggers = provider.alarmTriggers();
        assertEquals( 7, triggers.size() );
        int result = 0;
        for( String trigger : triggers )
        {
            if( Alarm.TRIGGER_ACTIVATE.equals( trigger ) )
            {
                result |= 1;
            }
            if( Alarm.TRIGGER_DEACTIVATE.equals( trigger ) )
            {
                result |= 2;
            }
            if( Alarm.TRIGGER_ACKNOWLEDGE.equals( trigger ) )
            {
                result |= 4;
            }
            if( Alarm.TRIGGER_BLOCK.equals( trigger ) )
            {
                result |= 8;
            }
            if( Alarm.TRIGGER_UNBLOCK.equals( trigger ) )
            {
                result |= 16;
            }
            if( Alarm.TRIGGER_DISABLE.equals( trigger ) )
            {
                result |= 32;
            }
            if( Alarm.TRIGGER_ENABLE.equals( trigger ) )
            {
                result |= 64;
            }
        }
        assertEquals( 127, result );
        assertEquals( Alarm.STATUS_NORMAL, underTest.currentStatus().name().get() );
    }

    @Test
    public void testStateChangeFromNormal()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        Alarm alarm = createAlarm( "Another 1" );
        AlarmEvent event1 = provider.evaluate( alarm, Alarm.TRIGGER_ACTIVATE );
        assertEquals( Alarm.EVENT_ACTIVATION, event1.systemName().get() );

        alarm = createAlarm( "Another 2" );
        AlarmEvent event2 = provider.evaluate( alarm, Alarm.TRIGGER_DEACTIVATE );
        assertNull( event2 );

        alarm = createAlarm( "Another 3" );
        AlarmEvent event3 = provider.evaluate( alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );

        alarm = createAlarm( "Another 4" );
        AlarmEvent event4 = provider.evaluate( alarm, Alarm.TRIGGER_BLOCK );
        assertEquals( Alarm.EVENT_BLOCKING, event4.systemName().get() );

        alarm = createAlarm( "Another 5" );
        AlarmEvent event5 = provider.evaluate( alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = createAlarm( "Another 6" );
        AlarmEvent event6 = provider.evaluate( alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( Alarm.EVENT_DISABLING, event6.systemName().get() );

        alarm = createAlarm( "Another 7" );
        AlarmEvent event7 = provider.evaluate( alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    @Test
    public void testStateChangeFromActivated()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        Alarm alarm = createAlarm( "Another 1" );
        alarm.activate();

        AlarmEvent event1 = provider.evaluate( alarm, Alarm.TRIGGER_ACTIVATE );
        assertNull( event1 );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        AlarmEvent event2 = provider.evaluate( alarm, Alarm.TRIGGER_DEACTIVATE );
        assertEquals( Alarm.EVENT_DEACTIVATION, event2.systemName().get() );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        AlarmEvent event3 = provider.evaluate( alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertEquals( Alarm.EVENT_ACKNOWLEDGEMENT, event3.systemName().get() );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        AlarmEvent event4 = provider.evaluate( alarm, Alarm.TRIGGER_BLOCK );
        assertEquals( Alarm.EVENT_BLOCKING, event4.systemName().get() );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        AlarmEvent event5 = provider.evaluate( alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        AlarmEvent event6 = provider.evaluate( alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( Alarm.EVENT_DISABLING, event6.systemName().get() );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        AlarmEvent event7 = provider.evaluate( alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    @Test
    public void testStateChangeFromAcknowledged()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        Alarm alarm = createAlarm( "Another 1" );
        alarm.activate();
        alarm.acknowledge();

        AlarmEvent event1 = provider.evaluate( alarm, Alarm.TRIGGER_ACTIVATE );
        assertNull( event1 );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event2 = provider.evaluate( alarm, Alarm.TRIGGER_DEACTIVATE );
        assertEquals( Alarm.EVENT_DEACTIVATION, event2.systemName().get() );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event3 = provider.evaluate( alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event4 = provider.evaluate( alarm, Alarm.TRIGGER_BLOCK );
        assertEquals( Alarm.EVENT_BLOCKING, event4.systemName().get() );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event5 = provider.evaluate( alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event6 = provider.evaluate( alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( Alarm.EVENT_DISABLING, event6.systemName().get() );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event7 = provider.evaluate( alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    @Test
    public void testStateChangeFromDeactivated()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        Alarm alarm = createAlarm( "Another 1" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event1 = provider.evaluate( alarm, Alarm.TRIGGER_ACTIVATE );
        assertEquals( Alarm.EVENT_ACTIVATION, event1.systemName().get() );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event2 = provider.evaluate( alarm, Alarm.TRIGGER_DEACTIVATE );
        assertNull( event2 );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event3 = provider.evaluate( alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertEquals( Alarm.EVENT_ACKNOWLEDGEMENT, event3.systemName().get() );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event4 = provider.evaluate( alarm, Alarm.TRIGGER_BLOCK );
        assertEquals( Alarm.EVENT_BLOCKING, event4.systemName().get() );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event5 = provider.evaluate( alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event6 = provider.evaluate( alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( Alarm.EVENT_DISABLING, event6.systemName().get() );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event7 = provider.evaluate( alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    @Test
    public void testStateChangeFromBlocked()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        Alarm alarm = createAlarm( "Another 1" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event1 = provider.evaluate( alarm, Alarm.TRIGGER_ACTIVATE );
        assertNull( event1 );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event2 = provider.evaluate( alarm, Alarm.TRIGGER_DEACTIVATE );
        assertNull( event2 );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event3 = provider.evaluate( alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event4 = provider.evaluate( alarm, Alarm.TRIGGER_BLOCK );
        assertNull( event4 );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event5 = provider.evaluate( alarm, Alarm.TRIGGER_UNBLOCK );
        assertEquals( Alarm.EVENT_UNBLOCKING, event5.systemName().get() );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event6 = provider.evaluate( alarm, Alarm.TRIGGER_DISABLE );
        assertEquals( Alarm.EVENT_DISABLING, event6.systemName().get() );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event7 = provider.evaluate( alarm, Alarm.TRIGGER_ENABLE );
        assertNull( event7 );
    }

    @Test
    public void testStateChangeFromDisabled()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        Alarm alarm = createAlarm( "Another 1" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event1 = provider.evaluate( alarm, Alarm.TRIGGER_ACTIVATE );
        assertNull( event1 );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event2 = provider.evaluate( alarm, Alarm.TRIGGER_DEACTIVATE );
        assertNull( event2 );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event3 = provider.evaluate( alarm, Alarm.TRIGGER_ACKNOWLEDGE );
        assertNull( event3 );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event4 = provider.evaluate( alarm, Alarm.TRIGGER_BLOCK );
        assertNull( event4 );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event5 = provider.evaluate( alarm, Alarm.TRIGGER_UNBLOCK );
        assertNull( event5 );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event6 = provider.evaluate( alarm, Alarm.TRIGGER_DISABLE );
        assertNull( event6 );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event7 = provider.evaluate( alarm, Alarm.TRIGGER_ENABLE );
        assertEquals( Alarm.EVENT_ENABLING, event7.systemName().get() );
    }

    @Test
    public void testIllegalTrigger()
        throws Exception
    {
        try
        {
            AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
            Alarm underTest = createAlarm( "Test Alarm" );
            provider.evaluate( underTest, "my-trigger" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e )
        {
            // Expected.
        }
    }

    @Test
    public void testNormalToActivated()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );
        underTest.activate();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_NORMAL, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACTIVATED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testActivatedToDeactivated()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );
        underTest.activate();
        underTest.deactivate();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_DEACTIVATED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testActivatedToAcknowledged()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.acknowledge();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACKNOWLEDGED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testDeactivatedToNormal()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.deactivate();
        underTest.acknowledge();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_DEACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_NORMAL, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testAcknowledgedToNormal()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.acknowledge();
        underTest.deactivate();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACKNOWLEDGED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_NORMAL, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testDisabledToNormal()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.trigger( "disable" );
        underTest.trigger( "enable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_DISABLED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_NORMAL, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testBlockedToNormal()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.trigger( "block" );
        underTest.trigger( "unblock" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_BLOCKED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_NORMAL, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testNormalToBlocked()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.trigger( "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_NORMAL, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_BLOCKED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testActivatedToBlocked()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.trigger( "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_BLOCKED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testDeactivatedToBlocked()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.deactivate();
        underTest.trigger( "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_DEACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_BLOCKED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testAcknowledgedToBlocked()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.acknowledge();
        underTest.trigger( "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACKNOWLEDGED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_BLOCKED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testNormalToDisabled()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_NORMAL, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_DISABLED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testActivatedToDisabled()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_DISABLED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testDeactivatedToDisabled()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.deactivate();
        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_DEACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_DISABLED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testAcknowledgedToDisabled()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.acknowledge();
        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACKNOWLEDGED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_DISABLED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testBlockedToDisabled()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.trigger( "block" );
        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_BLOCKED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_DISABLED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testDisabledToBlocked()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.activate();
        underTest.trigger( "disable" );
        underTest.trigger( "block" );       // This trigger should be ignored.
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_DISABLED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testConditionChanges1()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.updateCondition( false );
        AlarmEvent event = underTest.history().lastEvent();
        assertNull( "Generated an event but should have not.", event );
    }

    @Test
    public void testConditionChanges2()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.updateCondition( true );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_NORMAL, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACTIVATED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testConditionChanges3()
        throws Exception
    {
        Alarm underTest = createAlarm( "Test Alarm" );

        underTest.updateCondition( true );
        underTest.updateCondition( false );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        Assert.assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        Assert.assertEquals( Alarm.STATUS_DEACTIVATED, newstate.name().get() );

        Alarm eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testComputeCondition()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus s1 = createStatus( Alarm.STATUS_NORMAL );
        assertFalse( provider.computeCondition( s1 ) );
        AlarmStatus s2 = createStatus( Alarm.STATUS_ACTIVATED );
        assertTrue( provider.computeCondition( s2 ) );
        AlarmStatus s3 = createStatus( Alarm.STATUS_DEACTIVATED );
        assertFalse( provider.computeCondition( s3 ) );
        AlarmStatus s4 = createStatus( Alarm.STATUS_ACKNOWLEDGED );
        assertTrue( provider.computeCondition( s4 ) );

        AlarmStatus s5 = createStatus( Alarm.STATUS_DISABLED );
        assertFalse( provider.computeCondition( s5 ) );
        AlarmStatus s6 = createStatus( Alarm.STATUS_BLOCKED );
        assertFalse( provider.computeCondition( s6 ) );
        AlarmStatus s7 = createStatus( Alarm.STATUS_REACTIVATED );
        assertTrue( provider.computeCondition( s7 ) );
    }

    @Test
    public void testComputeTriggerNormal()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( Alarm.STATUS_NORMAL );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertEquals( Alarm.TRIGGER_ACTIVATE, trigger1 );
        assertEquals( null, trigger2 );
    }

    @Test
    public void testComputeTriggerActivated()
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( Alarm.STATUS_ACTIVATED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertEquals( null, trigger1 );
        assertEquals( Alarm.TRIGGER_DEACTIVATE, trigger2 );
    }

    @Test
    public void testComputeTRiggerDeactivated()
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( Alarm.STATUS_DEACTIVATED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertEquals( Alarm.TRIGGER_ACTIVATE, trigger1 );
        assertEquals( null, trigger2 );
    }

    @Test
    public void testComputeTriggerAcknowledged()
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( Alarm.STATUS_ACKNOWLEDGED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertEquals( null, trigger1 );
        assertEquals( Alarm.TRIGGER_DEACTIVATE, trigger2 );
    }

    @Test
    public void testComputeTriggerReactivated()
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( Alarm.STATUS_REACTIVATED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertEquals( null, trigger1 );
        assertEquals( Alarm.TRIGGER_DEACTIVATE, trigger2 );
    }

    @Test
    public void testComputeTriggerBlocked()
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( Alarm.STATUS_BLOCKED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertEquals( null, trigger1 );
        assertEquals( null, trigger2 );
    }

    @Test
    public void testComputeTriggerDisabled()
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( Alarm.STATUS_DISABLED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertEquals( null, trigger1 );
        assertEquals( null, trigger2 );
    }

    private Alarm createAlarm( String name )
    {
        UnitOfWork uow = module.currentUnitOfWork();
        EntityBuilder<Alarm> builder = uow.newEntityBuilder( Alarm.class );
        builder.instance().category().set( createCategory( "Testing" ) );
        Alarm.AlarmState state = builder.instanceFor( Alarm.AlarmState.class );
        state.currentStatus().set( createStatus( Alarm.STATUS_NORMAL ) );
        state.description().set( "Test Description" );
        state.systemName().set( name );
        return builder.newInstance();
    }

    private AlarmCategory createCategory( String name )
    {
        ValueBuilder<AlarmCategory> builder = module.newValueBuilder( AlarmCategory.class );
        builder.prototype().name().set( name );
        return builder.newInstance();
    }

    private Alarm getAlarm( String identity )
    {
        UnitOfWork uow = module.currentUnitOfWork();
        return uow.get( Alarm.class, identity );
    }

    private AlarmStatus createStatus( String status )
    {
        ValueBuilder<AlarmStatus> builder = module.newValueBuilder( AlarmStatus.class );
        builder.prototype().name().set( status );
        builder.prototype().creationDate().set( new Date() );
        return builder.newInstance();
    }
}
