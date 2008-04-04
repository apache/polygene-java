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

import org.junit.Test;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.Constraints;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.library.framework.constraint.annotation.NotNull;
import org.qi4j.library.framework.constraint.NotNullConstraint;
import org.qi4j.library.framework.validation.Validatable;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.AssemblyException;
import java.util.Collection;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class ConstraintsTest extends AbstractQi4jTest
{
    @Test
    public void testSingleConstraintOnMethod()
        throws Throwable
    {
        MyOne my = compositeBuilderFactory.newComposite( MyOne.class );
        my.doSomething( "habba" );
        try
        {
            my.doSomething( null );
            fail( "Should have thrown a ConstraintViolationException." );
        } catch( ConstraintViolationException e )
        {
            Collection<ConstraintViolation> violations = e.constraintViolations();
            assertEquals( 1, violations.size() );
            assertEquals( MyOneComposite.class.getName(), e.compositeType() );
        }
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( MyOneComposite.class );
    }

    @Constraints( NotNullConstraint.class )
    @Mixins( MyOneMixin.class )
    public interface MyOneComposite extends MyOne, Composite
    {}

    public interface MyOne
    {
        void doSomething( @NotNull String abc );
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
}
