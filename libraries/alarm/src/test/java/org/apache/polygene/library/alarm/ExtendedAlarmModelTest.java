/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.alarm;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

public class ExtendedAlarmModelTest
    extends AbstractPolygeneTest
{

    @SuppressWarnings( { "unchecked" } )
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( TestAlarmModel.class );
        module.services( AlarmSystemService.class );
        new EntityTestAssembler().assemble( module );
        module.entities( AlarmPointEntity.class );
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
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        unitOfWorkFactory.newUnitOfWork();
    }

    @Override
    public void tearDown()
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
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
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        boolean test1 = provider.modelDescription().toLowerCase().contains( "normal" );
        boolean test2 = provider.modelDescription().toLowerCase().contains( "activated" );
        boolean test3 = provider.modelDescription().toLowerCase().contains( "deactivated" );
        boolean test4 = provider.modelDescription().toLowerCase().contains( "acknowledged" );
        boolean test5 = provider.modelDescription().toLowerCase().contains( "activation" );
        boolean test6 = provider.modelDescription().toLowerCase().contains( "deactivation" );
        boolean test7 = provider.modelDescription().toLowerCase().contains( "acknowledge" );
        boolean test8 = provider.modelDescription().toLowerCase().contains( "block" );
        boolean test9 = provider.modelDescription().toLowerCase().contains( "unblock" );
        boolean test10 = provider.modelDescription().toLowerCase().contains( "disable" );
        boolean test11 = provider.modelDescription().toLowerCase().contains( "enable" );
        assertThat( test1 && test2 && test3 && test4 && test5 && test6 && test7 && test8 && test9 && test10 && test11, is( true ) );

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
        assertThat( test1 && test2 && test3 && test4 && test5 && test6 && test7 && test8 && test9 && test10 && test11, is( true ) );
    }

    @Test
    public void testTriggers()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        List<String> triggers = provider.alarmTriggers();
        assertThat( triggers.size(), equalTo( 7 ) );
        int result = 0;
        for( String trigger : triggers )
        {
            if( AlarmPoint.TRIGGER_ACTIVATE.equals( trigger ) )
            {
                result |= 1;
            }
            if( AlarmPoint.TRIGGER_DEACTIVATE.equals( trigger ) )
            {
                result |= 2;
            }
            if( AlarmPoint.TRIGGER_ACKNOWLEDGE.equals( trigger ) )
            {
                result |= 4;
            }
            if( AlarmPoint.TRIGGER_BLOCK.equals( trigger ) )
            {
                result |= 8;
            }
            if( AlarmPoint.TRIGGER_UNBLOCK.equals( trigger ) )
            {
                result |= 16;
            }
            if( AlarmPoint.TRIGGER_DISABLE.equals( trigger ) )
            {
                result |= 32;
            }
            if( AlarmPoint.TRIGGER_ENABLE.equals( trigger ) )
            {
                result |= 64;
            }
        }
        assertThat( result, equalTo( 127 ) );
        assertThat( underTest.currentStatus().name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );
    }

    @Test
    public void testStateChangeFromNormal()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( event1.systemName().get(), equalTo( AlarmPoint.EVENT_ACTIVATION ) );

        alarm = createAlarm( "Another 2" );
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( event2, nullValue() );

        alarm = createAlarm( "Another 3" );
        AlarmEvent event3 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACKNOWLEDGE );
        assertThat( event3, nullValue() );

        alarm = createAlarm( "Another 4" );
        AlarmEvent event4 = provider.evaluate( alarm, AlarmPoint.TRIGGER_BLOCK );
        assertThat( event4.systemName().get(), equalTo( AlarmPoint.EVENT_BLOCKING ) );

        alarm = createAlarm( "Another 5" );
        AlarmEvent event5 = provider.evaluate( alarm, AlarmPoint.TRIGGER_UNBLOCK );
        assertThat( event5, nullValue() );

        alarm = createAlarm( "Another 6" );
        AlarmEvent event6 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DISABLE );
        assertThat( event6.systemName().get(), equalTo( AlarmPoint.EVENT_DISABLING ) );

        alarm = createAlarm( "Another 7" );
        AlarmEvent event7 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ENABLE );
        assertThat( event7, nullValue() );
    }

    @Test
    public void testStateChangeFromActivated()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        alarm.activate();

        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( event1, nullValue() );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( event2.systemName().get(), equalTo( AlarmPoint.EVENT_DEACTIVATION ) );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        AlarmEvent event3 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACKNOWLEDGE );
        assertThat( event3.systemName().get(), equalTo( AlarmPoint.EVENT_ACKNOWLEDGEMENT ) );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        AlarmEvent event4 = provider.evaluate( alarm, AlarmPoint.TRIGGER_BLOCK );
        assertThat( event4.systemName().get(), equalTo( AlarmPoint.EVENT_BLOCKING ) );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        AlarmEvent event5 = provider.evaluate( alarm, AlarmPoint.TRIGGER_UNBLOCK );
        assertThat( event5, nullValue() );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        AlarmEvent event6 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DISABLE );
        assertThat( event6.systemName().get(), equalTo( AlarmPoint.EVENT_DISABLING ) );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        AlarmEvent event7 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ENABLE );
        assertThat( event7, nullValue() );
    }

    @Test
    public void testStateChangeFromAcknowledged()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        alarm.activate();
        alarm.acknowledge();

        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( event1, nullValue() );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( event2.systemName().get(), equalTo( AlarmPoint.EVENT_DEACTIVATION ) );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event3 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACKNOWLEDGE );
        assertThat( event3, nullValue() );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event4 = provider.evaluate( alarm, AlarmPoint.TRIGGER_BLOCK );
        assertThat( event4.systemName().get(), equalTo( AlarmPoint.EVENT_BLOCKING ) );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event5 = provider.evaluate( alarm, AlarmPoint.TRIGGER_UNBLOCK );
        assertThat( event5, nullValue() );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event6 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DISABLE );
        assertThat( event6.systemName().get(), equalTo( AlarmPoint.EVENT_DISABLING ) );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        alarm.acknowledge();
        AlarmEvent event7 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ENABLE );
        assertThat( event7, nullValue() );
    }

    @Test
    public void testStateChangeFromDeactivated()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( event1.systemName().get(), equalTo( AlarmPoint.EVENT_ACTIVATION ) );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( event2, nullValue() );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event3 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACKNOWLEDGE );
        assertThat( event3.systemName().get(), equalTo( AlarmPoint.EVENT_ACKNOWLEDGEMENT ) );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event4 = provider.evaluate( alarm, AlarmPoint.TRIGGER_BLOCK );
        assertThat( event4.systemName().get(), equalTo( AlarmPoint.EVENT_BLOCKING ) );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event5 = provider.evaluate( alarm, AlarmPoint.TRIGGER_UNBLOCK );
        assertThat( event5, nullValue() );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event6 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DISABLE );
        assertThat( event6.systemName().get(), equalTo( AlarmPoint.EVENT_DISABLING ) );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        alarm.deactivate();
        AlarmEvent event7 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ENABLE );
        assertThat( event7, nullValue() );
    }

    @Test
    public void testStateChangeFromBlocked()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( event1, nullValue() );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( event2, nullValue() );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event3 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACKNOWLEDGE );
        assertThat( event3, nullValue() );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event4 = provider.evaluate( alarm, AlarmPoint.TRIGGER_BLOCK );
        assertThat( event4, nullValue() );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event5 = provider.evaluate( alarm, AlarmPoint.TRIGGER_UNBLOCK );
        assertThat( event5.systemName().get(), equalTo( AlarmPoint.EVENT_UNBLOCKING ) );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event6 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DISABLE );
        assertThat( event6.systemName().get(), equalTo( AlarmPoint.EVENT_DISABLING ) );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        alarm.trigger( "block" );
        AlarmEvent event7 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ENABLE );
        assertThat( event7, nullValue() );
    }

    @Test
    public void testStateChangeFromDisabled()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( event1, nullValue() );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( event2, nullValue() );

        alarm = createAlarm( "Another 3" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event3 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACKNOWLEDGE );
        assertThat( event3, nullValue() );

        alarm = createAlarm( "Another 4" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event4 = provider.evaluate( alarm, AlarmPoint.TRIGGER_BLOCK );
        assertThat( event4, nullValue() );

        alarm = createAlarm( "Another 5" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event5 = provider.evaluate( alarm, AlarmPoint.TRIGGER_UNBLOCK );
        assertThat( event5, nullValue() );

        alarm = createAlarm( "Another 6" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event6 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DISABLE );
        assertThat( event6, nullValue() );

        alarm = createAlarm( "Another 7" );
        alarm.activate();
        alarm.trigger( "disable" );
        AlarmEvent event7 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ENABLE );
        assertThat( event7.systemName().get(), equalTo( AlarmPoint.EVENT_ENABLING ) );
    }

    @Test
    public void testIllegalTrigger()
        throws Exception
    {
        try
        {
            AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
            AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
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
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        underTest.activate();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testActivatedToDeactivated()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        underTest.activate();
        underTest.deactivate();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_DEACTIVATED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testActivatedToAcknowledged()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.acknowledge();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_ACKNOWLEDGED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testDeactivatedToNormal()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.deactivate();
        underTest.acknowledge();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_DEACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testAcknowledgedToNormal()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.acknowledge();
        underTest.deactivate();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACKNOWLEDGED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testDisabledToNormal()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.trigger( "disable" );
        underTest.trigger( "enable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_DISABLED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testBlockedToNormal()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.trigger( "block" );
        underTest.trigger( "unblock" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_BLOCKED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testNormalToBlocked()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.trigger( "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_BLOCKED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testActivatedToBlocked()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.trigger( "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_BLOCKED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testDeactivatedToBlocked()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.deactivate();
        underTest.trigger( "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_DEACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_BLOCKED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testAcknowledgedToBlocked()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.acknowledge();
        underTest.trigger( "block" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACKNOWLEDGED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_BLOCKED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testNormalToDisabled()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_DISABLED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testActivatedToDisabled()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_DISABLED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testDeactivatedToDisabled()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.deactivate();
        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_DEACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_DISABLED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testAcknowledgedToDisabled()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.acknowledge();
        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACKNOWLEDGED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_DISABLED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testBlockedToDisabled()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.trigger( "block" );
        underTest.trigger( "disable" );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_BLOCKED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_DISABLED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testDisabledToBlocked()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.activate();
        underTest.trigger( "disable" );
        underTest.trigger( "block" );       // This trigger should be ignored.
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_DISABLED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testConditionChanges1()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.updateCondition( false );
        AlarmEvent event = underTest.history().lastEvent();
        assertThat( "Generated an event but should have not.", event, nullValue() );
    }

    @Test
    public void testConditionChanges2()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.updateCondition( true );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testConditionChanges3()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );

        underTest.updateCondition( true );
        underTest.updateCondition( false );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_DEACTIVATED ) );

        AlarmPoint eventalarm = getAlarm( event.identity().get() );
        assertThat( eventalarm, equalTo( underTest ) );
    }

    @Test
    public void testComputeCondition()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus s1 = createStatus( AlarmPoint.STATUS_NORMAL );
        assertThat( provider.computeCondition( s1 ), is( false ) );
        AlarmStatus s2 = createStatus( AlarmPoint.STATUS_ACTIVATED );
        assertThat( provider.computeCondition( s2 ), is( true ) );
        AlarmStatus s3 = createStatus( AlarmPoint.STATUS_DEACTIVATED );
        assertThat( provider.computeCondition( s3 ), is( false ) );
        AlarmStatus s4 = createStatus( AlarmPoint.STATUS_ACKNOWLEDGED );
        assertThat( provider.computeCondition( s4 ), is( true ) );

        AlarmStatus s5 = createStatus( AlarmPoint.STATUS_DISABLED );
        assertThat( provider.computeCondition( s5 ), is( false ) );
        AlarmStatus s6 = createStatus( AlarmPoint.STATUS_BLOCKED );
        assertThat( provider.computeCondition( s6 ), is( false ) );
        AlarmStatus s7 = createStatus( AlarmPoint.STATUS_REACTIVATED );
        assertThat( provider.computeCondition( s7 ), is( true ) );
    }

    @Test
    public void testComputeTriggerNormal()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( AlarmPoint.STATUS_NORMAL );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertThat( trigger1, equalTo( AlarmPoint.TRIGGER_ACTIVATE ) );
        assertThat( trigger2, equalTo( null ) );
    }

    @Test
    public void testComputeTriggerActivated()
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( AlarmPoint.STATUS_ACTIVATED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertThat( trigger1, equalTo( null ) );
        assertThat( trigger2, equalTo( AlarmPoint.TRIGGER_DEACTIVATE ) );
    }

    @Test
    public void testComputeTRiggerDeactivated()
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( AlarmPoint.STATUS_DEACTIVATED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertThat( trigger1, equalTo( AlarmPoint.TRIGGER_ACTIVATE ) );
        assertThat( trigger2, equalTo( null ) );
    }

    @Test
    public void testComputeTriggerAcknowledged()
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( AlarmPoint.STATUS_ACKNOWLEDGED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertThat( trigger1, equalTo( null ) );
        assertThat( trigger2, equalTo( AlarmPoint.TRIGGER_DEACTIVATE ) );
    }

    @Test
    public void testComputeTriggerReactivated()
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( AlarmPoint.STATUS_REACTIVATED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertThat( trigger1, equalTo( null ) );
        assertThat( trigger2, equalTo( AlarmPoint.TRIGGER_DEACTIVATE ) );
    }

    @Test
    public void testComputeTriggerBlocked()
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( AlarmPoint.STATUS_BLOCKED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertThat( trigger1, equalTo( null ) );
        assertThat( trigger2, equalTo( null ) );
    }

    @Test
    public void testComputeTriggerDisabled()
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus status = createStatus( AlarmPoint.STATUS_DISABLED );
        String trigger1 = provider.computeTrigger( status, true );
        String trigger2 = provider.computeTrigger( status, false );
        assertThat( trigger1, equalTo( null ) );
        assertThat( trigger2, equalTo( null ) );
    }

    private AlarmPoint createAlarm( String name )
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
        EntityBuilder<AlarmPoint> builder = uow.newEntityBuilder( AlarmPoint.class );
        builder.instance().category().set( createCategory( "Testing" ) );
        AlarmPoint.AlarmState state = builder.instanceFor( AlarmPoint.AlarmState.class );
        state.currentStatus().set( createStatus( AlarmPoint.STATUS_NORMAL ) );
        state.description().set( "Test Description" );
        state.systemName().set( name );
        return builder.newInstance();
    }

    private AlarmCategory createCategory( String name )
    {
        ValueBuilder<AlarmCategory> builder = valueBuilderFactory.newValueBuilder( AlarmCategory.class );
        builder.prototype().name().set( name );
        return builder.newInstance();
    }

    private AlarmPoint getAlarm( Identity identity )
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
        return uow.get( AlarmPoint.class, identity );
    }

    private AlarmStatus createStatus( String status )
    {
        ValueBuilder<AlarmStatus> builder = valueBuilderFactory.newValueBuilder( AlarmStatus.class );
        AlarmStatus.State statePrototype = builder.prototypeFor( AlarmStatus.State.class );
        statePrototype.name().set( status );
        statePrototype.creationDate().set( Instant.now() );
        return builder.newInstance();
    }
}
