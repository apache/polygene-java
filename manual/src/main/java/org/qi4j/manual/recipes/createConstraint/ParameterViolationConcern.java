/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.manual.recipes.createConstraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;

// START SNIPPET: report
public class ParameterViolationConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        try
        {
            return next.invoke( proxy, method, args );
        }
        catch( ConstraintViolationException e )
        {
            for( ConstraintViolation violation : e.constraintViolations() )
            {
                String name = violation.name();
                Object value = violation.value();
                Annotation constraint = violation.constraint();
                report( name, value, constraint );
            }
            throw new IllegalArgumentException("Invalid argument(s)", e);
        }
    }

// END SNIPPET: report
// START SNIPPET: report
    private void report( String name, Object value, Annotation constraint )
    {
    }
}
// END SNIPPET: report
