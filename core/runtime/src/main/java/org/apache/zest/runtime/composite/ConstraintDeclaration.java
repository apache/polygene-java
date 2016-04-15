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

package org.apache.zest.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.apache.zest.api.constraint.Constraint;
import org.apache.zest.api.util.Classes;

/**
 * JAVADOC
 */
public final class ConstraintDeclaration
{
    private final Class<? extends Constraint<?, ?>> constraintClass;
    @SuppressWarnings( "raw" )
    private final Class constraintAnnotationType;
    private final Type constraintValueType;

    @SuppressWarnings( "unchecked" )
    public ConstraintDeclaration( Class<? extends Constraint<?, ?>> constraintClass )
    {
        this.constraintClass = constraintClass;

        constraintAnnotationType = (Class<? extends Annotation>) ( (ParameterizedType) constraintClass.getGenericInterfaces()[ 0 ] )
            .getActualTypeArguments()[ 0 ];
        constraintValueType = ( (ParameterizedType) constraintClass.getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 1 ];
    }

    public Class<? extends Constraint<?, ?>> constraintClass()
    {
        return constraintClass;
    }

    @SuppressWarnings( {"raw", "unchecked"} )
    public boolean appliesTo( Class annotationType, Type valueType )
    {
        if( constraintValueType instanceof Class )
        {
            Class constraintValueClass = (Class) constraintValueType;
            Class valueClass = Classes.RAW_CLASS.apply( valueType );
            return constraintAnnotationType.equals( annotationType ) && constraintValueClass.isAssignableFrom( valueClass );
        }
        else if( constraintValueType instanceof ParameterizedType )
        {
            // TODO Handle nested generics
            Class constraintValueClass = Classes.RAW_CLASS.apply( constraintValueType );
            Class valueClass = Classes.RAW_CLASS.apply( valueType );
            return constraintAnnotationType.equals( annotationType ) && constraintValueClass.isAssignableFrom( valueClass );
        }
        else
        {
            return false; // TODO Handles more cases. What are they?
        }
    }
}
