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
    private String name;

    public ValueConstraintsInstance( List<AbstractConstraintModel> constraintModels, String name )
    {
        this.name = name;
        constraints = new ArrayList<ConstraintInstance>();
        for( AbstractConstraintModel constraintModel : constraintModels )
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
                    violations = new ArrayList<ConstraintViolation>();
                }
                ConstraintViolation violation = new ConstraintViolation( name, constraint.annotation(), value );
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
