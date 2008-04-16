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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Constraint;
import org.qi4j.composite.ConstraintDeclaration;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.Constraints;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.ParameterConstraintViolationException;
import org.qi4j.test.AbstractQi4jTest;


public class ConstraintsTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( MyOneComposite.class );
        module.addComposites( MyOneComposite2.class );
    }

    @Test
    public void givenCompositeWithConstraintsWhenInstantiatedThenUseDeclarationOnComposite()
        throws Throwable
    {
        MyOne my = compositeBuilderFactory.newComposite( MyOneComposite.class );
        my.doSomething( "habba" );
        try
        {
            my.doSomething( null );
            fail( "Should have thrown a ParameterConstraintViolationException." );
        }
        catch( ParameterConstraintViolationException e )
        {
            Collection<ConstraintViolation> violations = e.constraintViolations();
            assertEquals( 1, violations.size() );
            assertEquals( MyOneComposite.class.getName(), e.compositeType() );
        }
    }

    @Test
    public void givenCompositeWithoutConstraintsWhenInstantiatedThenUseDeclarationOnConstraint()
        throws Throwable
    {
        MyOne my = compositeBuilderFactory.newComposite( MyOneComposite.class );
        my.doSomething( "habba" );
        try
        {
            my.doSomething( null );
            fail( "Should have thrown a ParameterConstraintViolationException." );
        }
        catch( ParameterConstraintViolationException e )
        {
            Collection<ConstraintViolation> violations = e.constraintViolations();
            assertEquals( 1, violations.size() );
            assertEquals( MyOneComposite.class.getName(), e.compositeType() );
        }
    }

    @Constraints( TestConstraintImpl.class )
    @Mixins( MyOneMixin.class )
    public interface MyOneComposite extends MyOne, Composite
    {
    }

    @Mixins( MyOneMixin.class )
    public interface MyOneComposite2 extends MyOne, Composite
    {
    }

    public interface MyOne
    {
        void doSomething( @TestConstraint String abc );
    }

    public static class MyOneMixin
        implements MyOne
    {

        public void doSomething( String abc )
        {
            if( abc == null )
            {
                throw new NullPointerException();
            }
        }
    }

    @ConstraintDeclaration
    @Retention( RetentionPolicy.RUNTIME )
    @Constraints( TestConstraintImpl.class )
    public @interface TestConstraint
    {
    }

    public static class TestConstraintImpl
        implements Constraint<TestConstraint, Object>
    {
        public boolean isValid( TestConstraint annotation, Object value ) throws NullPointerException
        {
            return value != null;
        }
    }
}
