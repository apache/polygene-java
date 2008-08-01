/*
 * Copyright 2008 Georg Ragaller. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.library.constraints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.test.AbstractQi4jTest;

public class ContstraintTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestCaseComposite.class );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testContainsFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().containsString().set( "bar" );
    }

    @Test
    public void testContainsOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().notNullObject().set( "foo" );
        cb.stateOfComposite().notNullObject().set( "xxxfooyyy" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testEmailFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().email().set( "foo.com" );
    }

    @Test
    public void testEmailOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().email().set( "rickard@gmail.com" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testGreaterThanFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().greaterThan().set( 10 );
    }

    @Test
    public void testGreaterThanOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().greaterThan().set( 11 );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testInstanceOfFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().instanceOf().set( new HashSet() );
    }

    @Test
    public void testInstanceOfOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().instanceOf().set( new ArrayList() );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testLessThanFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().lessThan().set( 10 );
    }

    @Test
    public void testLessThanOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().lessThan().set( 9 );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testMatchesFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().matches().set( "cba" );
    }

    @Test
    public void testMatchesOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().matches().set( "abbccc" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testMaxLengthFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().maxLength().set( "xxxxx" );
    }

    @Test
    public void testMaxLengthOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().maxLength().set( "xxx" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testMinLengthFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().minLength().set( "xx" );
    }

    @Test
    public void testMinLengthOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().minLength().set( "xxx" );
    }

    @Test
    public void testNotEmptyFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );
        try
        {
            cb.stateOfComposite().notEmptyString().set( "" );
            fail( "Should have thrown exception" );
        }
        catch( ConstraintViolationException e )
        {
        }

        try
        {
            cb.stateOfComposite().notEmptyCollection().set( new ArrayList() );
            fail( "Should have thrown exception" );
        }
        catch( ConstraintViolationException e )
        {
        }

        try
        {
            cb.stateOfComposite().notEmptyList().set( new ArrayList() );
            fail( "Should have thrown exception" );
        }
        catch( ConstraintViolationException e )
        {
        }
    }

    @Test
    public void testNotEmptyOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );
        cb.stateOfComposite().notEmptyString().set( "X" );
        cb.stateOfComposite().notEmptyCollection().set( Arrays.asList( "X" ) );
        cb.stateOfComposite().notEmptyList().set( Arrays.asList( "X" ) );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testNotNullFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().notNullObject().set( null );
    }

    @Test
    public void testNotNullOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().notNullObject().set( new Object() );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testRangeFail()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().range().set( 101 );
    }

    @Test
    public void testRangeOk()
    {
        CompositeBuilder<TestCaseComposite> cb = compositeBuilderFactory.newCompositeBuilder( TestCaseComposite.class );

        cb.stateOfComposite().range().set( 0 );
        cb.stateOfComposite().range().set( 50 );
        cb.stateOfComposite().range().set( 100 );
    }

}