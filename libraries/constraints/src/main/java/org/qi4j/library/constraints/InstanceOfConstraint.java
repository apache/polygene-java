/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.library.constraints;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.InstanceOf;

/**
 * Implement @InstanceOf constraint.
 */
public class InstanceOfConstraint
    implements Constraint<InstanceOf, Object>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( InstanceOf annotation, Object parameter )
        throws NullPointerException
    {
        if( parameter != null )
        {
            for( Class aClass : annotation.value() )
            {
                if( !aClass.isInstance( parameter ) )
                {
                    return false;
                }
            }
        }
        return true;
    }

}
