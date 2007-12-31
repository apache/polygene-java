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

package org.qi4j.spi.composite;

import java.lang.annotation.Annotation;
import org.qi4j.composite.Constraint;

/**
 * A ConstraintModel matches each use
 * of the @Constraints annotation. One model is created
 * for each class in the @Constraints annotation.
 */
public final class ConstraintModel<T>
{
    private Class<? extends Constraint> constraintType;
    private Class<? extends Annotation> annotationType;
    private Class<T> parameterType;
    private Class declaredBy;

    public ConstraintModel( Class<? extends Constraint> constraintType, Class<? extends Annotation> annotationType, Class<T> parameterType, Class declaredBy )
    {
        this.declaredBy = declaredBy;
        this.constraintType = constraintType;
        this.annotationType = annotationType;
        this.parameterType = parameterType;
    }

    public Class<? extends Constraint> getConstraintType()
    {
        return constraintType;
    }

    public Class<? extends Annotation> getAnnotationType()
    {
        return annotationType;
    }

    public Class<T> getParameterType()
    {
        return parameterType;
    }

    public Class getDeclaredBy()
    {
        return declaredBy;
    }


    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        ConstraintModel that = (ConstraintModel) o;

        if( !constraintType.equals( that.constraintType ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return constraintType.hashCode();
    }


    @Override public String toString()
    {
        return "@" + annotationType.getName() + " for " + parameterType.getName() + " implemented by " + constraintType.getName();
    }
}
