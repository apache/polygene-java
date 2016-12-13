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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.constraint.ConstraintViolation;
import org.apache.polygene.api.constraint.ConstraintViolationException;

/**
 * JAVADOC
 */
public final class ConstraintsInstance
{
    private final List<ValueConstraintsInstance> valueConstraintsInstances;

    public ConstraintsInstance( List<ValueConstraintsInstance> parameterConstraints )
    {
        valueConstraintsInstances = parameterConstraints;
    }

    @SuppressWarnings( "unchecked" )
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
                    violations = new ArrayList<>();
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
                throw new ConstraintViolationException( ( (CompositeInstance) instance ).proxy(), method, violations );
            }
            Stream<Class<?>> types = Stream.of( instance.getClass() );
            throw new ConstraintViolationException( instance.toString(), types, method, violations );
        }
    }
}
