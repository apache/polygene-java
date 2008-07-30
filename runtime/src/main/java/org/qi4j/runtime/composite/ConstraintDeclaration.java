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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.composite.Constraint;

/**
 * TODO
 */
public final class ConstraintDeclaration
{
    private final Class<? extends Constraint<?, ?>> constraintClass;
    private final Type declaredIn;
    private final Class constraintAnnotationType;
    private final Type constraintValueType;

    public ConstraintDeclaration( Class<? extends Constraint<?, ?>> constraintClass, Type type )
    {
        this.constraintClass = constraintClass;
        declaredIn = type;

        constraintAnnotationType = (Class<? extends Annotation>) ( (ParameterizedType) constraintClass.getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 0 ];
        constraintValueType = ( (ParameterizedType) constraintClass.getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 1 ];
    }

    public Class<? extends Constraint<?, ?>> constraintClass()
    {
        return constraintClass;
    }

    public Type declaredIn()
    {
        return declaredIn;
    }

    public boolean appliesTo( Class annotationType, Type valueType )
    {
        if( constraintValueType instanceof Class )
        {
            Class constraintValueClass = (Class) constraintValueType;
            Class valueClass = extractRawInjectionClass( valueType );
            return constraintAnnotationType.equals( annotationType ) && constraintValueClass.isAssignableFrom( valueClass );
        }
        else if( constraintValueType instanceof ParameterizedType )
        {
            // TODO Handle nested generics
            Class constraintValueClass = extractRawInjectionClass( constraintValueType );
            Class valueClass = extractRawInjectionClass( valueType );
            return constraintAnnotationType.equals( annotationType ) && constraintValueClass.isAssignableFrom( valueClass );
        }
        else
        {
            return false; // TODO Handles more cases. What are they?
        }
    }

    private Class<?> extractRawInjectionClass( final Type injectionType )
    {
        // Calculate raw injection type
        if( injectionType instanceof Class )
        {
            return (Class<?>) injectionType;
        }
        else if( injectionType instanceof ParameterizedType )
        {
            return (Class<?>) ( (ParameterizedType) injectionType ).getRawType();
        }
        throw new IllegalArgumentException( "Could not extract the raw class of " + injectionType );
    }
}
