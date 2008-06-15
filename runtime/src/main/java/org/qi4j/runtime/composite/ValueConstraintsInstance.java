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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.qi4j.composite.ConstraintViolation;

/**
 * TODO
 */
public final class ValueConstraintsInstance
{
    private final List<ConstraintInstance> constraints;

    public ValueConstraintsInstance( List<ConstraintModel> constraintModels )
    {
        constraints = new ArrayList<ConstraintInstance>();
        for( ConstraintModel constraintModel : constraintModels )
        {
            ConstraintInstance instance = constraintModel.newInstance();
            constraints.add( instance );
        }
    }

    public List<ConstraintViolation> checkConstraints( Object value )
    {
        List<ConstraintViolation> violations = null;
        for( ConstraintInstance constraint : constraints )
        {
            if( !constraint.isValid( value ) )
            {
                if( violations == null )
                {
                    violations = new ArrayList<ConstraintViolation>();
                }
                ConstraintViolation violation = new ConstraintViolation( constraint.annotation(), value );
                violations.add( violation );
            }
        }

        if( violations == null )
        {
            violations = Collections.emptyList();
        }

        return violations;
    }
}
