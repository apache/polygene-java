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

package org.apache.polygene.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.constraint.ValueConstraintViolation;
import org.apache.polygene.api.constraint.ConstraintViolationException;

public final class ValueConstraintsInstance
{
    private static final Optional OPTIONAL;

    static
    {
        OPTIONAL = new OptionalDummy();
    }

    private final List<ConstraintInstance> constraints;
    private String name;
    private boolean optional;

    public ValueConstraintsInstance( List<AbstractConstraintModel> constraintModels, String name, boolean optional )
    {
        this.name = name;
        this.optional = optional;
        constraints = new ArrayList<>();
        for( AbstractConstraintModel constraintModel : constraintModels )
        {
            constraints.add( constraintModel.newInstance() );
        }
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    public List<ValueConstraintViolation> checkConstraints( Object value )
    {
        List<ValueConstraintViolation> violations = null;

        // Check optional first - this avoids NPE's in constraints
        if( optional )
        {
            if( value == null )
            {
                violations = Collections.emptyList();
            }
        }
        else
        {
            if( value == null )
            {
                violations = new ArrayList<>();
                violations.add( new ValueConstraintViolation( name, OPTIONAL, null ) );
            }
        }

        if( violations == null )
        {
            for( ConstraintInstance constraint : constraints )
            {
                boolean valid;
                try
                {
                    valid = constraint.isValid( value );
                }
                catch( NullPointerException e )
                {
                    // A NPE is the same as a failing constraint
                    valid = false;
                }

                if( !valid )
                {
                    if( violations == null )
                    {
                        violations = new ArrayList<>();
                    }
                    ValueConstraintViolation violation = new ValueConstraintViolation( name, constraint.annotation(), value );
                    violations.add( violation );
                }
            }
        }
        if( violations == null )
        {
            violations = Collections.emptyList();
        }
        return violations;
    }

    public void checkConstraints( Object value, AccessibleObject accessor )
    {
        List<ValueConstraintViolation> violations = checkConstraints( value );
        if( !violations.isEmpty() )
        {
            for( ValueConstraintViolation violation : violations )
            {
                if( accessor instanceof Member )
                {
                    Member member = (Member) accessor;
                    String methodName =  member.getName();
                    violation.setMixinType( member.getDeclaringClass() );
                    violation.setMethodName( methodName );
                }

            }
            throw new ConstraintViolationException( violations );
        }
    }

    @SuppressWarnings( "AnnotationAsSuperInterface" )
    private static class OptionalDummy
        implements Optional
    {
        @Override
        public Class<? extends Annotation> annotationType()
        {
            return Optional.class;
        }

        @Override
        public String toString()
        {
            return "not optional";
        }
    }
}
