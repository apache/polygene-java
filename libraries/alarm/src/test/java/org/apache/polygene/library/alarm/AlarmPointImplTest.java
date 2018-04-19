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

import java.util.List;
import java.util.Locale;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

public class AlarmPointImplTest extends AbstractPolygeneTest
    implements AlarmListener
{
    private int fired;
    private AlarmSystem alarmSystem;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( TestAlarmModel.class );
        module.services( AlarmSystemService.class );
        module.entities( AlarmPointEntity.class );
        module.values( AlarmEvent.class );
        module.values( AlarmCategory.class );
        module.values( AlarmStatus.class );
        new EntityTestAssembler().assemble( module );
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
    @AfterEach
    public void tearDown()
    {
        if( unitOfWorkFactory.isUnitOfWorkActive() )
        {
            UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
            uow.discard();
        }
        super.tearDown();
    }

    @Test
    public void testCreationProblems()
        throws Exception
    {
        try
        {
            createAlarm( null );
            fail( "AlarmPoint created with null name." );
        }
        catch( ConstraintViolationException e )
        {
            // expected.
        }
        try
        {
            createAlarm( "" );
            fail( "AlarmPoint created with empty string name." );
        }
        catch( ConstraintViolationException e )
        {
            // expected.
        }
        try
        {
            createAlarm( "\n \n" );
            fail( "AlarmPoint created with white space name." );
        }
        catch( ConstraintViolationException e )
        {
            // expected.
        }
    }

    @Test
    public void testName()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "TestCase AlarmPoint" );
        assertThat( underTest.name(), equalTo( "TestCase AlarmPoint" ) );
    }

    @Test
    public void testDescription()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "TestCase AlarmPoint" );
//        assertThat( underTest.description() , equalTo( "This is a default Locale description of a testcase AlarmPoint."));

        Locale swedish = new Locale( "sv" );
        assertThat( underTest.description( swedish ), equalTo( "Detta \u00E5r en svensk beskrivning av ett testlarm." ) );

        Locale english = Locale.UK;
        assertThat( underTest.description( english ), equalTo( "This is a UK Locale description of a testcase Alarm." ) );
    }

    @Test
    public void testState()
    {
        AlarmPoint underTest = createAlarm( "testState" );
        assertThat( underTest.currentStatus().name( null ), equalTo( AlarmPoint.STATUS_NORMAL ) );
        boolean condition = underTest.currentCondition();
        assertThat( condition, equalTo( false ) );
    }

    @Test
    public void testAttributes()
    {
        AlarmPoint underTest = createAlarm( "TestCase AlarmPoint" );

        String alarmText = underTest.attribute( "text" );
        assertThat( alarmText, nullValue() );

        underTest.setAttribute( "text", "TestCase AlarmPoint" );
        assertThat( underTest.attribute( "text" ), equalTo( "TestCase AlarmPoint" ) );

        List<String> names = underTest.attributeNames();
        assertThat( names.size(), equalTo( 1 ) );

        underTest.setAttribute( "text", null );
        names = underTest.attributeNames();
        assertThat( names.size(), equalTo( 0 ) );
    }

    @Test
    public void testInvalidTrigger()
        throws Exception
    {
        try
        {
            AlarmPoint underTest = createAlarm( "TestCase AlarmPoint" );
            underTest.trigger( "my-special-trigger" );
            fail( "IllegalArgumentException was not thrown." );
        }
        catch( IllegalArgumentException e )
        {
            // Expected.
        }
    }

    @Test
    public void testNoEvent()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "TestCase AlarmPoint" );
        alarmSystem.addAlarmListener( this );
        underTest.deactivate();
        assertThat( fired, equalTo( 0 ) );
    }

    @Test
    public void testListener()
    {
        AlarmPoint underTest = createAlarm( "TestCase AlarmPoint" );
        try
        {
            alarmSystem.removeAlarmListener( null );  // make sure it fails.
            fail( "Could remove null listener" );
        }
        catch( ConstraintViolationException e )
        {
            // expected
        }
        alarmSystem.removeAlarmListener( this );  // make sure it doesn't fail.

        alarmSystem.addAlarmListener( this );
        underTest.activate();
        assertThat( fired, equalTo( 1 ) );
        alarmSystem.addAlarmListener( this );
        underTest.deactivate();
        assertThat( fired, equalTo( 3 ) );
        alarmSystem.removeAlarmListener( this );
        underTest.activate();
        assertThat( fired, equalTo( 4 ) );
        alarmSystem.removeAlarmListener( this );
        underTest.deactivate();
        assertThat( fired, equalTo( 4 ) );
    }

    public void alarmFired( AlarmEvent event )
    {
        fired++;
    }

    private AlarmPoint createAlarm( String name )
    {
        ServiceReference<AlarmSystem> ref = serviceFinder.findService( AlarmSystem.class );
        alarmSystem = ref.get();
        return alarmSystem.createAlarm( name, createCategory( "AlarmPointImplTest" ) );
    }

    private AlarmCategory createCategory( String name )
    {
        ValueBuilder<AlarmCategory> builder = valueBuilderFactory.newValueBuilder( AlarmCategory.class );
        builder.prototype().name().set( name );
        return builder.newInstance();
    }
}
