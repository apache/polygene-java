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

import java.util.List;
import java.util.Locale;
import org.junit.Test;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class AlarmPointImplTest extends AbstractQi4jTest
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
        if( module.isUnitOfWorkActive() )
        {
            UnitOfWork uow = module.currentUnitOfWork();
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
        assertEquals( "TestCase AlarmPoint", underTest.name() );
    }

    @Test
    public void testDescription()
        throws Exception
    {
        AlarmPoint underTest = createAlarm( "TestCase AlarmPoint" );
//        assertEquals( "This is a default Locale description of a testcase AlarmPoint.", underTest.description() );

        Locale swedish = new Locale( "sv" );
        assertEquals( "Detta \u00E5r en svensk beskrivning av ett testlarm.", underTest.description( swedish ) );

        Locale english = Locale.UK;
        assertEquals( "This is a UK Locale description of a testcase Alarm.", underTest.description( english ) );
    }

    @Test
    public void testState()
    {
        AlarmPoint underTest = createAlarm( "testState" );
        assertEquals( AlarmPoint.STATUS_NORMAL, underTest.currentStatus().name( null ) );
        boolean condition = underTest.currentCondition();
        assertEquals( false, condition );
    }

    @Test
    public void testAttributes()
    {
        AlarmPoint underTest = createAlarm( "TestCase AlarmPoint" );

        String alarmText = underTest.attribute( "text" );
        assertNull( alarmText );

        underTest.setAttribute( "text", "TestCase AlarmPoint" );
        assertEquals( "TestCase AlarmPoint", underTest.attribute( "text" ) );

        List<String> names = underTest.attributeNames();
        assertEquals( 1, names.size() );

        underTest.setAttribute( "text", null );
        names = underTest.attributeNames();
        assertEquals( 0, names.size() );
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
        assertEquals( 0, fired );
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
        assertEquals( 1, fired );
        alarmSystem.addAlarmListener( this );
        underTest.deactivate();
        assertEquals( 3, fired );
        alarmSystem.removeAlarmListener( this );
        underTest.activate();
        assertEquals( 4, fired );
        alarmSystem.removeAlarmListener( this );
        underTest.deactivate();
        assertEquals( 4, fired );
    }

    public void alarmFired( AlarmEvent event )
    {
        fired++;
    }

    private AlarmPoint createAlarm( String name )
    {
        ServiceReference<AlarmSystem> ref = module.findService( AlarmSystem.class );
        alarmSystem = ref.get();
        return alarmSystem.createAlarm( name, createCategory( "AlarmPointImplTest" ) );
    }

    private AlarmCategory createCategory( String name )
    {
        ValueBuilder<AlarmCategory> builder = module.newValueBuilder( AlarmCategory.class );
        builder.prototype().name().set( name );
        return builder.newInstance();
    }
}
