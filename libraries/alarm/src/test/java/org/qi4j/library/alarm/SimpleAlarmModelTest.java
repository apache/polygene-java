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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SimpleAlarmModelTest
    extends AbstractQi4jTest
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
        module.values( AlarmEvent.class );
        module.values( AlarmCategory.class );
        module.values( AlarmStatus.class );
        module.forMixin( AlarmHistory.class ).declareDefaults().maxSize().set( 10 );
    }

    @Mixins( SimpleAlarmModelService.SimpleAlarmModelMixin.class )
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
        SimpleAlarmModelService.SimpleAlarmModelMixin spi = new SimpleAlarmModelService.SimpleAlarmModelMixin();
        assertEquals( "org.qi4j.library.alarm.model.simple", spi.modelName() );
    }

    @Test
    public void testDescription()
        throws Exception
    {
        SimpleAlarmModelService.SimpleAlarmModelMixin spi = new SimpleAlarmModelService.SimpleAlarmModelMixin();
        boolean test1 = spi.modelDescription().toLowerCase().contains( "normal" );
        boolean test2 = spi.modelDescription().toLowerCase().contains( "activated" );
        boolean test3 = spi.modelDescription().toLowerCase().contains( "activation" );
        boolean test4 = spi.modelDescription().toLowerCase().contains( "deactivation" );
        assertTrue( test1 && test2 && test3 && test4 );

        Locale english = new Locale( "en" );
        test1 = spi.modelDescription( english ).toLowerCase().contains( "normal" );
        test2 = spi.modelDescription( english ).toLowerCase().contains( "activated" );
        test3 = spi.modelDescription( english ).toLowerCase().contains( "activation" );
        test4 = spi.modelDescription( english ).toLowerCase().contains( "deactivation" );
        assertTrue( test1 && test2 && test3 && test4 );
    }

    @Test
    public void testTriggers()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        List<String> triggers = provider.alarmTriggers();
        assertEquals( 2, triggers.size() );
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
            if( AlarmPoint.TRIGGER_ENABLE.equals( trigger ) )
            {
                result |= 32;
            }
            if( AlarmPoint.TRIGGER_DISABLE.equals( trigger ) )
            {
                result |= 64;
            }
        }
        assertEquals( 3, result );
        assertEquals( AlarmPoint.STATUS_NORMAL, underTest.currentStatus().name(null) );
    }

    @Test
    public void testStateChangeFromNormal()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertEquals( AlarmPoint.EVENT_ACTIVATION, event1.systemName().get() );

        alarm = createAlarm( "Another 2" );
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertNull( event2 );

        try
        {
            alarm = createAlarm( "Another 3" );
            AlarmEvent event3 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACKNOWLEDGE );
            assertNull( event3 );
            fail( "[Acknowledge] trigger should not be allowed on this model." );
        }
        catch( IllegalArgumentException e )
        {
            // expected
        }
    }

    @Test
    public void testStateChangeFromActivated()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        alarm.activate();

        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertNull( event1 );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertEquals( AlarmPoint.EVENT_DEACTIVATION, event2.systemName().get() );
    }

    @Test
    public void testIllegalTrigger()
        throws Exception
    {
        try
        {
            AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
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
        assertEquals( AlarmPoint.STATUS_NORMAL, oldstate.name(null) );

        AlarmStatus newstate = event.newStatus().get();
        assertEquals( AlarmPoint.STATUS_ACTIVATED, newstate.name(null) );

        AlarmPoint eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testActivatedToNormal()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        underTest.activate();
        underTest.deactivate();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertEquals( AlarmPoint.STATUS_ACTIVATED, oldstate.name(null) );

        AlarmStatus newstate = event.newStatus().get();
        assertEquals( AlarmPoint.STATUS_NORMAL, newstate.name(null) );

        AlarmPoint eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testConditionChanges1()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        underTest.updateCondition( false );
        AlarmEvent event = underTest.history().lastEvent();
        assertNull( "Generated an event but should have not.", event );
    }

    @Test
    public void testConditionChanges2()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        underTest.updateCondition( true );
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertEquals( AlarmPoint.STATUS_NORMAL, oldstate.name(null) );

        AlarmStatus newstate = event.newStatus().get();
        assertEquals( AlarmPoint.STATUS_ACTIVATED, newstate.name(null) );

        AlarmPoint eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
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
        assertEquals( AlarmPoint.STATUS_ACTIVATED, oldstate.name(null) );

        AlarmStatus newstate = event.newStatus().get();
        assertEquals( AlarmPoint.STATUS_NORMAL, newstate.name(null) );

        AlarmPoint eventalarm = getAlarm( event.alarmIdentity().get() );
        assertEquals( underTest, eventalarm );
    }

    @Test
    public void testComputeCondition()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus s1 = createStatus( AlarmPoint.STATUS_NORMAL );
        assertFalse( provider.computeCondition( s1 ) );
        AlarmStatus s2 = createStatus( AlarmPoint.STATUS_ACTIVATED );
        assertTrue( provider.computeCondition( s2 ) );
    }

    @Test
    public void testComputeTrigger()
        throws Exception
    {
        AlarmModel provider = (AlarmModel) module.findService( AlarmModel.class ).get();
        AlarmStatus s1 = createStatus( AlarmPoint.STATUS_NORMAL );
        AlarmStatus s2 = createStatus( AlarmPoint.STATUS_ACTIVATED );
        String trigger1 = provider.computeTrigger( s1, true );
        String trigger2 = provider.computeTrigger( s2, true );
        String trigger5 = provider.computeTrigger( s1, false );
        String trigger6 = provider.computeTrigger( s2, false );
        assertEquals( AlarmPoint.TRIGGER_ACTIVATE, trigger1 );
        assertEquals( null, trigger2 );
        assertEquals( null, trigger5 );
        assertEquals( AlarmPoint.TRIGGER_DEACTIVATE, trigger6 );
    }

    private AlarmPoint createAlarm( String name )
    {
        UnitOfWork uow = module.currentUnitOfWork();
        EntityBuilder<AlarmPoint> builder = uow.newEntityBuilder( AlarmPoint.class );
        builder.instance().category().set( createCategory( "SimpleModelTest" ) );
        AlarmPoint.AlarmState state = builder.instanceFor( AlarmPoint.AlarmState.class );
        state.currentStatus().set( createStatus( AlarmPoint.STATUS_NORMAL ) );
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

    private AlarmPoint getAlarm( String identity )
    {
        UnitOfWork uow = module.currentUnitOfWork();
        return uow.get( AlarmPoint.class, identity );
    }

    private AlarmStatus createStatus( String status )
    {
        ValueBuilder<AlarmStatus> builder = module.newValueBuilder( AlarmStatus.class );
        AlarmStatus.State statePrototype = builder.prototypeFor( AlarmStatus.State.class );
        statePrototype.name().set( status );
        statePrototype.creationDate().set( new Date() );
        return builder.newInstance();
    }
}
