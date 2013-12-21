/*
 * Copyright 2005-2011 Niclas Hedhman.
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

import org.junit.Test;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import java.util.Map;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.*;

public class AlarmHistoryImplTest
    extends AbstractQi4jTest
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
        module.newUnitOfWork();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        if (module.isUnitOfWorkActive())
        {
            UnitOfWork uow = module.currentUnitOfWork();
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
        assertNull( event1 );
        assertNull( event2 );
        assertEquals( "Activate Counter", 0, hist.activateCounter() );
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
        assertFalse( event1.equals( event2 ) );
        assertEquals( AlarmPoint.STATUS_ACTIVATED, event1.newStatus().get().name(null) );
        assertEquals( AlarmPoint.STATUS_NORMAL, event2.newStatus().get().name(null) );
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

        assertEquals( 5, eventCounter );
        assertEquals( 5, hist.allAlarmEvents().get().size() );

        AlarmEvent event = hist.eventAt( -1 );
        assertNull( event );
        event = hist.eventAt( 5 );
        assertNull( event );
        event = hist.eventAt( 0 );
        assertEquals( "activation", event.systemName().get() );
        event = hist.eventAt( 1 );
        assertEquals( "deactivation", event.systemName().get() );
        event = hist.eventAt( 2 );
        assertEquals( "activation", event.systemName().get() );
        event = hist.eventAt( 3 );
        assertEquals( "deactivation", event.systemName().get() );
        event = hist.eventAt( 4 );
        assertEquals( "activation", event.systemName().get() );
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

        assertEquals( 5, eventCounter );
        assertEquals( 5, hist.allAlarmEvents().get().size() );

        AlarmEvent event = hist.eventAtEnd( -1 );
        assertNull( event );
        event = hist.eventAtEnd( 5 );
        assertNull( event );
        event = hist.eventAtEnd( 4 );
        assertEquals( "activation", event.systemName().get() );
        event = hist.eventAtEnd( 3 );
        assertEquals( "deactivation", event.systemName().get() );
        event = hist.eventAtEnd( 2 );
        assertEquals( "activation", event.systemName().get() );
        event = hist.eventAtEnd( 1 );
        assertEquals( "deactivation", event.systemName().get() );
        event = hist.eventAtEnd( 0 );
        assertEquals( "activation", event.systemName().get() );
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
        assertEquals( 3, activateCounters );

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
            assertEquals( 0, c1 );
        }
        else
        {
            assertEquals( c1, n1 );
        }

        if( n2 == null )
        {
            assertEquals( 0, c2 );
        }
        else
        {
            assertEquals( c2, n2 );
        }
    }

    @Test
    public void testSetMaxSize()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "testSetMaxSize" );
        alarmSystem.addAlarmListener( this );
        AlarmHistory hist = underTest.history();
        assertEquals( 0, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertEquals( 1, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        assertEquals( 2, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertEquals( 3, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        assertEquals( 4, hist.allAlarmEvents().get().size() );

        int maxsize = hist.maxSize().get();
        assertEquals( 30, maxsize );

        hist.maxSize().set( 3 );    // The Qi4j version doesn't intercept the maxSize().set() method and purge the old
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE ); // so we do another event to purge.
        assertEquals( 3, hist.allAlarmEvents().get().size() );

        hist.maxSize().set( 0 );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE ); // so we do another event to purge.
        assertEquals( 0, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertEquals( 0, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        assertEquals( 0, hist.allAlarmEvents().get().size() );
        hist.maxSize().set( 2 );
        assertEquals( 0, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertEquals( 1, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        assertEquals( 2, hist.allAlarmEvents().get().size() );
        underTest.trigger( AlarmPoint.TRIGGER_ACTIVATE );
        assertEquals( 2, hist.allAlarmEvents().get().size() );
        assertEquals( 11, eventCounter );
    }

    public void alarmFired( AlarmEvent event )
    {
        eventCounter++;
    }

    private AlarmPoint createAlarm( String name )
    {
        ServiceReference<AlarmSystem> ref = module.findService( AlarmSystem.class );
        alarmSystem = ref.get();
        return alarmSystem.createAlarm( name, createCategory( "AlarmHistoryTest" ) );
    }

    private AlarmCategory createCategory( String name )
    {
        ValueBuilder<AlarmCategory> builder = module.newValueBuilder( AlarmCategory.class );
        builder.prototype().name().set( name );
        return builder.newInstance();
    }


}
