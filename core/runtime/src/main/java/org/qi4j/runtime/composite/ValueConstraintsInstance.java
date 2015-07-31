/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.ConstraintViolation;

/**
 * JAVADOC
 */
public final class ValueConstraintsInstance
{
    private static final Optional OPTIONAL;

    static
    {
        OPTIONAL = new OptionalDummy();
    }

    @SuppressWarnings( "raw" )
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

    @SuppressWarnings( {"raw", "unchecked"} )
    public List<ConstraintViolation> checkConstraints( Object value )
    {
        List<ConstraintViolation> violations = null;

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
                violations.add( new ConstraintViolation( name, OPTIONAL, null ) );
            }
        }

        if( violations == null && value != null )
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
                    ConstraintViolation violation = new ConstraintViolation( name, constraint.annotation(), value );
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
