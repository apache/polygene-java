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
package org.apache.polygene.constraints;

import java.util.Collection;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.constraint.Constraints;
import org.apache.polygene.api.constraint.ValueConstraintViolation;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.constraints.MinLengthConstraint;
import org.apache.polygene.library.constraints.annotation.Matches;
import org.apache.polygene.library.constraints.annotation.MinLength;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

public class PropertyConstraintTest
    extends AbstractPolygeneTest
{
    @Test
    public void givenConstraintOnPropertyWhenInvalidValueThenThrowException()
        throws Throwable
    {
        TransientBuilder<TestType> builder = transientBuilderFactory.newTransientBuilder( TestType.class );
        builder.prototype().test().set( "XXXXXX" );
        TestType test = builder.newInstance();
        try
        {
            test.test().set( "YY" );
            fail( "Should have thrown a ConstraintViolationException." );
        }
        catch( ConstraintViolationException e )
        {
            Collection<ValueConstraintViolation> violations = e.constraintViolations();
            assertThat( violations.size(), equalTo( 2 ) );
        }
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite.class );
    }

    @Constraints( { MinLengthConstraint.class } )
    public interface TestComposite
        extends TestType, TransientComposite
    {
    }

    public interface TestType
    {
        @MinLength( 3 )
        @Matches( "X*" )
        Property<String> test();
    }
}