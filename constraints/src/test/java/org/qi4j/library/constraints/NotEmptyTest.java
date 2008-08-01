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
import static org.junit.Assert.fail;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.test.AbstractQi4jTest;

public class NotEmptyTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestCaseComposite.class );
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
}
