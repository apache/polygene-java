/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.api.constraint;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.rest.server.api.ObjectSelection;

/**
 * Annotation on interactions that requires objects of specific types
 * to be available in the ObjectSelection.
 *
 * Example:
 * <pre>
 *     &#64;Requires(File.class)
 *     public Representation content() {...}
 * </pre>
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( Requires.RequiresRoleConstraint.class )
public @interface Requires
{
    Class<?>[] value();

    class RequiresRoleConstraint
        implements Constraint<Requires, ObjectSelection>
    {
        @Override
        public boolean isValid( Requires requires, ObjectSelection objectSelection )
        {
            for( Class<?> roleClass : requires.value() )
            {
                try
                {
                    objectSelection.get( roleClass );
                }
                catch( IllegalArgumentException ex )
                {
                    return false;
                }
            }
            return true;
        }
    }
}
