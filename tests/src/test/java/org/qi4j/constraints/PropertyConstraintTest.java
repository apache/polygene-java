/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.constraints;

import java.util.Collection;
import static org.junit.Assert.*;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.composite.Constraints;
import org.qi4j.library.framework.constraint.MinLengthConstraint;
import org.qi4j.library.framework.constraint.NotNullConstraint;
import org.qi4j.library.framework.constraint.annotation.MinLength;
import org.qi4j.library.framework.constraint.annotation.NotNull;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;


public class PropertyConstraintTest extends AbstractQi4jTest
{
    @org.junit.Test
    public void givenConstraintOnPropertyWhenInvalidValueThenThrowException()
        throws Throwable
    {
        Test test = compositeBuilderFactory.newComposite( Test.class );
        try
        {
            test.test().set( null );
            fail( "Should have thrown a ConstraintViolationException." );
        }
        catch( ConstraintViolationException e )
        {
            Collection<ConstraintViolation> violations = e.constraintViolations();
            assertEquals( 2, violations.size() );

            System.out.println( e.getLocalizedMessage() );
        }
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestComposite.class );
    }

    @Constraints( { NotNullConstraint.class, MinLengthConstraint.class } )
    public interface TestComposite extends Test, Composite
    {
    }

    public interface Test
    {
        @NotNull @MinLength( 3 ) Property<String> test();
    }
}