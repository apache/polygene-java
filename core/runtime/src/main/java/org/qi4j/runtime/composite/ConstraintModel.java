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
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.constraint.Constraint;

/**
 * JAVADOC
 */
public final class ConstraintModel
    extends AbstractConstraintModel
{
    private final Class<? extends Constraint<?, ?>> constraintClass;

    public ConstraintModel( Annotation annotation, Class<? extends Constraint<?, ?>> constraintClass )
    {
        super( annotation );
        this.constraintClass = constraintClass;
    }

    @Override
    public ConstraintInstance<?, ?> newInstance()
    {
        try
        {
            Constraint<?, ?> constraint = constraintClass.newInstance();
            return new ConstraintInstance( constraint, annotation );
        }
        catch( Exception e )
        {
            throw new ConstructionException( "Could not instantiate constraint implementation", e );
        }
    }
}
