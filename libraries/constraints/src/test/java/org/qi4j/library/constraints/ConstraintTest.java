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
import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.fail;

public class ConstraintTest
    extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestCaseComposite.class );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testContainsFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().containsString().set( "bar" );
    }

    @Test
    public void testContainsOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().containsString().set( "foo" );
        cb.prototype().containsString().set( "xxxfooyyy" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testEmailFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().email().set( "foo.com" );
    }

    @Test
    public void testEmailOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().email().set( "rickard@gmail.com" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testURLFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().url().set( "this is no url" );
    }

    @Test
    public void testURLOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().url().set( "http://qi4j.org/path?query=string#fragment" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testURIFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().uri().set( "" );
    }

    @Test
    public void testURIOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().uri().set( "http://qi4j.org/path?query=string#fragment" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testGreaterThanFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().greaterThan().set( 10 );
    }

    @Test
    public void testGreaterThanOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().greaterThan().set( 11 );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testInstanceOfFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().instanceOf().set( new HashSet() );
    }

    @Test
    public void testInstanceOfOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().instanceOf().set( new ArrayList() );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testLessThanFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().lessThan().set( 10 );
    }

    @Test
    public void testLessThanOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().lessThan().set( 9 );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testMatchesFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().matches().set( "cba" );
    }

    @Test
    public void testMatchesOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().matches().set( "abbccc" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testMaxLengthFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().maxLength().set( "xxxxx" );
    }

    @Test
    public void testMaxLengthOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().maxLength().set( "xxx" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testMinLengthFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().minLength().set( "xx" );
    }

    @Test
    public void testMinLengthOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().minLength().set( "xxx" );
    }

    @Test
    public void testNotEmptyFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );
        try
        {
            cb.prototype().notEmptyString().set( "" );
            fail( "Should have thrown exception" );
        }
        catch( ConstraintViolationException e )
        {
        }

        try
        {
            cb.prototype().notEmptyCollection().set( new ArrayList() );
            fail( "Should have thrown exception" );
        }
        catch( ConstraintViolationException e )
        {
        }

        try
        {
            cb.prototype().notEmptyList().set( new ArrayList() );
            fail( "Should have thrown exception" );
        }
        catch( ConstraintViolationException e )
        {
        }
    }

    @Test
    public void testNotEmptyOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().notEmptyString().set( "X" );
        cb.prototype().notEmptyCollection().set( Arrays.asList( "X" ) );
        cb.prototype().notEmptyList().set( Arrays.asList( "X" ) );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testOneOfFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().oneOf().set( "Foo" );
    }

    @Test
    public void testOneOfOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().oneOf().set( "Bar" );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testRangeFail()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().range().set( 101 );
    }

    @Test
    public void testRangeOk()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );

        cb.prototype().range().set( 0 );
        cb.prototype().range().set( 50 );
        cb.prototype().range().set( 100 );
    }

    @Test
    public void testMethodParameters()
    {
        TransientBuilder<TestCaseComposite> cb = module.newTransientBuilder( TestCaseComposite.class );
        cb.prototype().testParameters( 15 );
    }

}