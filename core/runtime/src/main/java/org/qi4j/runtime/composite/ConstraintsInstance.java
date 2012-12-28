/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2009, Niclas Hedhman. All Rights Reserved.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;

import static org.qi4j.functional.Iterables.iterable;

/**
 * JAVADOC
 */
public final class ConstraintsInstance
{
    private List<ValueConstraintsInstance> valueConstraintsInstances;

    public ConstraintsInstance( List<ValueConstraintsInstance> parameterConstraints )
    {
        valueConstraintsInstances = parameterConstraints;
    }

    public void checkValid( Object instance, Method method, Object[] params )
        throws ConstraintViolationException
    {
        if( valueConstraintsInstances.isEmpty() )
        {
            return; // No constraints to check
        }

        // Check constraints
        List<ConstraintViolation> violations = null;
        for( int i = 0; i < params.length; i++ )
        {
            Object param = params[ i ];
            List<ConstraintViolation> paramViolations = valueConstraintsInstances.get( i ).checkConstraints( param );
            if( !paramViolations.isEmpty() )
            {
                if( violations == null )
                {
                    violations = new ArrayList<ConstraintViolation>();
                }
                violations.addAll( paramViolations );
            }
        }

        // Check if any constraint failed
        if( violations != null )
        {
            if( instance instanceof Composite )
            {
                throw new ConstraintViolationException( (Composite) instance, method, violations );
            }
            if( instance instanceof CompositeInstance )
            {
                throw new ConstraintViolationException( (Composite) ( (CompositeInstance) instance ).proxy(), method, violations );
            }
            Iterable<? extends Class<?>> types = iterable( instance.getClass() );
            throw new ConstraintViolationException( instance.toString(), (Iterable<Class<?>>) types, method, violations );
        }
    }
}
