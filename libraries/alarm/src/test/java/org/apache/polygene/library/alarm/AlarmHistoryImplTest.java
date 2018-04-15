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

import java.util.Map;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

public class AlarmHistoryImplTest
    extends AbstractPolygeneTest
    implements AlarmListener
{
    private int eventCounter = 0;
    private AlarmSystem alarmSystem;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( TestAlarmModel.class );
        module.services( AlarmSystemService.class );
        module.entities( AlarmPointEntity.class );
        new EntityTestAssembler().assemble( module );
        module.values( AlarmStatus.class );
        module.values( AlarmCategory.class );
        module.values( AlarmEvent.class );
        module.entities( AlarmPointEntity.class );
        module.forMixin( AlarmHistory.class ).declareDefaults().maxSize().set( 30 );
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
        unitOfWorkFactory.newUnitOfWork();
    }

    @Override
    public void tearDown()
    {
        if ( unitOfWorkFactory.isUnitOfWorkActive())
        {
            UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
            uow.discard();
        }
        super.tearDown();
    }

    @Test
    public void testEmpty()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "testEmpty" );
        AlarmHistory hist = underTest.history();
        AlarmEvent event1 = hist.firstEvent();
        AlarmEvent event2 = hist.lastEvent();
        assertThat( event1, nullValue() );
        assertThat( event2, nullValue() );
        assertThat( "Activate Counter", hist.activateCounter(), equalTo( 0 ) );
    }

    @Test
    public void testFirstNotLast()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "testFirstNotLast" );
        underTest.updateCondition( true );
        underTest.updateCondition( false );
        AlarmHistory hist = underTest.history();
        AlarmEvent event1 = hist.firstEvent();
        AlarmEvent event2 = hist.lastEvent();
        assertThat( event1.equals( event2 ), is( false ) );
        assertThat( event1.newStatus().get().name( null ), equalTo( AlarmPoint.STATUS_ACTIVATED ) );
        assertThat( event2.newStatus().get().name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );
    }

    @Test
    public void testGetPosition()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "testGetPosition" );
        alarmSystem.addAlarmListener( this );
        AlarmHistory hist = underTest.history();
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );

        assertThat( eventCounter, equalTo( 5 ) );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 5 ) );

        AlarmEvent event = hist.eventAt( -1 );
        assertThat( event, nullValue() );
        event = hist.eventAt( 5 );
        assertThat( event, nullValue() );
        event = hist.eventAt( 0 );
        assertThat( event.systemName().get(), equalTo( "activation" ) );
        event = hist.eventAt( 1 );
        assertThat( event.systemName().get(), equalTo( "deactivation" ) );
        event = hist.eventAt( 2 );
        assertThat( event.systemName().get(), equalTo( "activation" ) );
        event = hist.eventAt( 3 );
        assertThat( event.systemName().get(), equalTo( "deactivation" ) );
        event = hist.eventAt( 4 );
        assertThat( event.systemName().get(), equalTo( "activation" ) );
    }

    @Test
    public void testGetPositionFromLast()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "testGetPositionFromLast" );
        alarmSystem.addAlarmListener( this );
        AlarmHistory hist = underTest.history();
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );

        assertThat( eventCounter, equalTo( 5 ) );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 5 ) );

        AlarmEvent event = hist.eventAtEnd( -1 );
        assertThat( event, nullValue() );
        event = hist.eventAtEnd( 5 );
        assertThat( event, nullValue() );
        event = hist.eventAtEnd( 4 );
        assertThat( event.systemName().get(), equalTo( "activation" ) );
        event = hist.eventAtEnd( 3 );
        assertThat( event.systemName().get(), equalTo( "deactivation" ) );
        event = hist.eventAtEnd( 2 );
        assertThat( event.systemName().get(), equalTo( "activation" ) );
        event = hist.eventAtEnd( 1 );
        assertThat( event.systemName().get(), equalTo( "deactivation" ) );
        event = hist.eventAtEnd( 0 );
        assertThat( event.systemName().get(), equalTo( "activation" ) );
    }

    @Test
    public void testCounters()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "testCounters" );
        AlarmHistory hist = underTest.history();
        Map<String, Integer> counters = hist.counters().get();

        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        verifyCounters( counters, 1, 0 );

        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        verifyCounters( counters, 1, 1 );

        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        verifyCounters( counters, 2, 1 );

        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        verifyCounters( counters, 2, 2 );

        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        verifyCounters( counters, 2, 2 );

        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        verifyCounters( counters, 3, 2 );

        int activateCounters = hist.activateCounter();
        assertThat( activateCounters, equalTo( 3 ) );

        hist.resetActivateCounter();
        verifyCounters( counters, 0, 2 );

        hist.resetAllCounters();
        verifyCounters( counters, 0, 0 );
    }

    private void verifyCounters( Map counters, int c1, int c2 )
    {
        Number n1 = (Number) counters.get( AlarmPoint.TRIGGER_ACTIVATE );
        Number n2 = (Number) counters.get( AlarmPoint.TRIGGER_DEACTIVATE );
        if( n1 == null )
        {
            assertThat( c1, equalTo( 0 ) );
        }
        else
        {
            assertThat( n1, equalTo( c1 ) );
        }

        if( n2 == null )
        {
            assertThat( c2, equalTo( 0 ) );
        }
        else
        {
            assertThat( n2, equalTo( c2 ) );
        }
    }

    @Test
    public void testSetMaxSize()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "testSetMaxSize" );
        alarmSystem.addAlarmListener( this );
        AlarmHistory hist = underTest.history();
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 0 ) );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 1 ) );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 2 ) );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 3 ) );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 4 ) );

        int maxsize = hist.maxSize().get();
        assertThat( maxsize, equalTo( 30 ) );

        hist.maxSize().set( 3 );    // The Polygene version doesn't intercept the maxSize().set() method and purge the old
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE ); // so we do another event to purge.
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 3 ) );

        hist.maxSize().set( 0 );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE ); // so we do another event to purge.
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 0 ) );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 0 ) );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 0 ) );
        hist.maxSize().set( 2 );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 0 ) );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 1 ) );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 2 ) );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertThat( hist.allAlarmEvents().get().size(), equalTo( 2 ) );
        assertThat( eventCounter, equalTo( 11 ) );
    }

    public void alarmFired( AlarmEvent event )
    {
        eventCounter++;
    }

    private AlarmPoint createAlarm( String name )
    {
        ServiceReference<AlarmSystem> ref = serviceFinder.findService( AlarmSystem.class );
        alarmSystem = ref.get();
        return alarmSystem.createAlarm( name, createCategory( "AlarmHistoryTest" ) );
    }

    private AlarmCategory createCategory( String name )
    {
        ValueBuilder<AlarmCategory> builder = valueBuilderFactory.newValueBuilder( AlarmCategory.class );
        builder.prototype().name().set( name );
        return builder.newInstance();
    }


}
