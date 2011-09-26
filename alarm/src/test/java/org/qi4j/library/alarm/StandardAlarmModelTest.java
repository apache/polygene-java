/*
 * Copyright 2005-2011 Niclas Hedhman.
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

import static org.junit.Assert.*;

public class StandardAlarmModelTest
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
        module.values( AlarmEvent.class );
        module.values( AlarmCategory.class );
        module.values( AlarmStatus.class );
        module.forMixin( AlarmHistory.class ).declareDefaults().maxSize().set( 10 );
    }

    @Mixins( StandardAlarmModelService.StandardAlarmModelMixin.class )
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
    public void testName()
        throws Exception
    {
        StandardAlarmModelService.StandardAlarmModelMixin spi = new StandardAlarmModelService.StandardAlarmModelMixin();
        assertEquals( "org.qi4j.library.alarm.model.standard", spi.modelName() );
    }

    @Test
    public void testDescription()
        throws Exception
    {
        StandardAlarmModelService.StandardAlarmModelMixin spi = new StandardAlarmModelService.StandardAlarmModelMixin();
        boolean test1 = spi.modelDescription().toLowerCase().contains( "normal" );
        boolean test2 = spi.modelDescription().toLowerCase().contains( "activated" );
        boolean test3 = spi.modelDescription().toLowerCase().contains( "deactivated" );
        boolean test4 = spi.modelDescription().toLowerCase().contains( "acknowledged" );
        boolean test5 = spi.modelDescription().toLowerCase().contains( "activation" );
        boolean test6 = spi.modelDescription().toLowerCase().contains( "deactivation" );
        boolean test7 = spi.modelDescription().toLowerCase().contains( "acknowledge" );
        assertTrue( test1 && test2 && test3 && test4 && test5 && test6 && test7 );

        Locale english = new Locale( "en" );
        test1 = spi.modelDescription( english ).toLowerCase().contains( "normal" );
        test2 = spi.modelDescription( english ).toLowerCase().contains( "activated" );
        test3 = spi.modelDescription( english ).toLowerCase().contains( "deactivated" );
        test4 = spi.modelDescription( english ).toLowerCase().contains( "acknowledged" );
        test5 = spi.modelDescription( english ).toLowerCase().contains( "activation" );
        test6 = spi.modelDescription( english ).toLowerCase().contains( "deactivation" );
        test7 = spi.modelDescription( english ).toLowerCase().contains( "acknowledge" );
        assertTrue( test1 && test2 && test3 && test4 && test5 && test6 && test7 );
    }

    @Test
    public void testTriggers()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        Alarm underTest = createAlarm( "Test Alarm" );
        List<String> triggers = provider.alarmTriggers();
        assertEquals( 3, triggers.size() );
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
        assertEquals( 7, result );
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
        assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

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
        assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

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
        assertEquals( Alarm.STATUS_DEACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        assertEquals( Alarm.STATUS_NORMAL, newstate.name().get() );

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
        assertEquals( Alarm.STATUS_ACKNOWLEDGED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        assertEquals( Alarm.STATUS_NORMAL, newstate.name().get() );

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
        assertEquals( Alarm.STATUS_NORMAL, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        assertEquals( Alarm.STATUS_ACTIVATED, newstate.name().get() );

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
        assertEquals( Alarm.STATUS_ACTIVATED, oldstate.name().get() );

        AlarmStatus newstate = event.newStatus().get();
        assertEquals( Alarm.STATUS_DEACTIVATED, newstate.name().get() );

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
    }

    @Test
    public void testComputeTrigger()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus s1 = createStatus( Alarm.STATUS_NORMAL );
        AlarmStatus s2 = createStatus( Alarm.STATUS_ACTIVATED );
        AlarmStatus s3 = createStatus( Alarm.STATUS_DEACTIVATED );
        AlarmStatus s4 = createStatus( Alarm.STATUS_ACKNOWLEDGED );
        String trigger1 = provider.computeTrigger( s1, true );
        String trigger2 = provider.computeTrigger( s2, true );
        String trigger3 = provider.computeTrigger( s3, true );
        String trigger4 = provider.computeTrigger( s4, true );
        String trigger5 = provider.computeTrigger( s1, false );
        String trigger6 = provider.computeTrigger( s2, false );
        String trigger7 = provider.computeTrigger( s3, false );
        String trigger8 = provider.computeTrigger( s4, false );
        assertEquals( Alarm.TRIGGER_ACTIVATE, trigger1 );
        assertEquals( null, trigger2 );
        assertEquals( Alarm.TRIGGER_ACTIVATE, trigger3 );
        assertEquals( null, trigger4 );
        assertEquals( null, trigger5 );
        assertEquals( Alarm.TRIGGER_DEACTIVATE, trigger6 );
        assertEquals( null, trigger7 );
        assertEquals( Alarm.TRIGGER_DEACTIVATE, trigger8 );
    }

    private Alarm createAlarm( String name )
    {
        UnitOfWork uow = module.currentUnitOfWork();
        EntityBuilder<Alarm> builder = uow.newEntityBuilder( Alarm.class );
        builder.instance().category().set( createCategory( "StandardModelTest" ) );
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
