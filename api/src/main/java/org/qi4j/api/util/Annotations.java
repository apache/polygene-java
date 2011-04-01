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

package org.qi4j.api.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.api.specification.Specification;

import static org.qi4j.api.util.Classes.*;
import static org.qi4j.api.util.Iterables.*;

/**
 * Useful methods for handling Annotations.
 */
public final class Annotations
{
    public static Specification<Annotation> hasAnnotation( final Class<? extends Annotation> annotationType )
    {
        return new Specification<Annotation>()
        {
            public boolean satisfiedBy( Annotation annotation )
            {
                return annotation.annotationType().getAnnotation( annotationType ) != null;
            }
        };
    }

    public static Specification<Annotation> isType( final Class<? extends Annotation> annotationType )
    {
        return new Specification<Annotation>()
        {
            public boolean satisfiedBy( Annotation annotation )
            {
                return annotation.annotationType().equals( annotationType );
            }
        };
    }

    public static <T extends Annotation> T getAnnotation( Type type, Class<T> annotationType )
    {
        if( !( type instanceof Class ) )
        {
            return null;
        }
        return annotationType.cast( ( (Class<?>) type ).getAnnotation( annotationType ) );
    }

    public static Iterable<Annotation> getMethodAndTypeAnnotations( Method method )
    {
        return flatten( iterable( method.getAnnotations() ),
                        flattenIterables( map( new Function<Class, Iterable<Annotation>>()
                        {
                            public Iterable<Annotation> map( Class aClass )
                            {
                                return iterable( aClass.getAnnotations() );
                            }
                        }, interfacesOf( method.getReturnType() ) ) ) );
    }
}
