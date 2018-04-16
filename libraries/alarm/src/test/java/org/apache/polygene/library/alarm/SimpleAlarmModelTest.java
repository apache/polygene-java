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

public class SimpleAlarmModelTest
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
    public void testName()
        throws Exception
    {
        SimpleAlarmModelService.SimpleAlarmModelMixin spi = new SimpleAlarmModelService.SimpleAlarmModelMixin();
        assertThat( spi.modelName(), equalTo( "org.apache.polygene.library.alarm.model.simple" ) );
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
        assertThat( test1 && test2 && test3 && test4, is( true ) );

        Locale english = new Locale( "en" );
        test1 = spi.modelDescription( english ).toLowerCase().contains( "normal" );
        test2 = spi.modelDescription( english ).toLowerCase().contains( "activated" );
        test3 = spi.modelDescription( english ).toLowerCase().contains( "activation" );
        test4 = spi.modelDescription( english ).toLowerCase().contains( "deactivation" );
        assertThat( test1 && test2 && test3 && test4, is( true ) );
    }

    @Test
    public void testTriggers()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        List<String> triggers = provider.alarmTriggers();
        assertThat( triggers.size(), equalTo( 2 ) );
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
        assertThat( result, equalTo( 3 ) );
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

        try
        {
            alarm = createAlarm( "Another 3" );
            AlarmEvent event3 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACKNOWLEDGE );
            assertThat( event3, nullValue() );
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
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmPoint alarm = createAlarm( "Another 1" );
        alarm.activate();

        AlarmEvent event1 = provider.evaluate( alarm, AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( event1, nullValue() );

        alarm = createAlarm( "Another 2" );
        alarm.activate();
        AlarmEvent event2 = provider.evaluate( alarm, AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( event2.systemName().get(), equalTo( AlarmPoint.EVENT_DEACTIVATION ) );
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
    public void testActivatedToNormal()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "Test AlarmPoint" );
        underTest.activate();
        underTest.deactivate();
        AlarmEvent event = underTest.history().lastEvent();

        AlarmStatus oldstate = event.oldStatus().get();
        assertThat( oldstate.name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );

        AlarmStatus newstate = event.newStatus().get();
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

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
        assertThat( newstate.name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );

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
    }

    @Test
    public void testComputeTrigger()
        throws Exception
    {
        AlarmModel provider = serviceFinder.findService( AlarmModel.class ).get();
        AlarmStatus s1 = createStatus( AlarmPoint.STATUS_NORMAL );
        AlarmStatus s2 = createStatus( AlarmPoint.STATUS_ACTIVATED );
        String trigger1 = provider.computeTrigger( s1, true );
        String trigger2 = provider.computeTrigger( s2, true );
        String trigger5 = provider.computeTrigger( s1, false );
        String trigger6 = provider.computeTrigger( s2, false );
        assertThat( trigger1, equalTo( AlarmPoint.TRIGGER_ACTIVATE ) );
        assertThat( trigger2, equalTo( null ) );
        assertThat( trigger5, equalTo( null ) );
        assertThat( trigger6, equalTo( AlarmPoint.TRIGGER_DEACTIVATE ) );
    }

    private AlarmPoint createAlarm( String name )
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
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
