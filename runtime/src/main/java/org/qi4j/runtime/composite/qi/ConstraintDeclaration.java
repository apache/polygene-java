/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.composite.Constraint;

/**
 * TODO
 */
public class ConstraintDeclaration
{
    private Class<? extends Constraint<?, ?>> constraintClass;
    private Type declaredIn;
    private Class constraintAnnotationType;
    private Class constraintValueType;

    public ConstraintDeclaration( Class<? extends Constraint<?, ?>> constraintClass, Type type )
    {
        this.constraintClass = constraintClass;
        declaredIn = type;

        constraintAnnotationType = (Class<? extends Annotation>) ( (ParameterizedType) constraintClass.getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 0 ];
        constraintValueType = (Class) ( (ParameterizedType) constraintClass.getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 1 ];
    }

    public Class<? extends Constraint<?, ?>> constraintClass()
    {
        return constraintClass;
    }

    public Type declaredIn()
    {
        return declaredIn;
    }

    public boolean appliesTo( Class annotationType, Class valueType )
    {
        return constraintAnnotationType.equals( annotationType ) && constraintValueType.isAssignableFrom( valueType );

    }
}
