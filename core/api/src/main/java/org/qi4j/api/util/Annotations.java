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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;

import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.api.util.Classes.typeOf;
import static org.qi4j.functional.Iterables.flatten;
import static org.qi4j.functional.Iterables.flattenIterables;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Iterables.map;

/**
 * Useful methods for handling Annotations.
 */
public final class Annotations
{
    public static Function<Type, Iterable<Annotation>> ANNOTATIONS_OF = Classes.forTypes( new Function<Type, Iterable<Annotation>>()
    {
        @Override
        public Iterable<Annotation> map( Type type )
        {
            return Iterables.iterable( Classes.RAW_CLASS.map( type ).getAnnotations() );
        }
    } );

    public static Specification<AnnotatedElement> hasAnnotation( final Class<? extends Annotation> annotationType )
    {
        return new Specification<AnnotatedElement>()
        {
            @Override
            public boolean satisfiedBy( AnnotatedElement element )
            {
                return element.getAnnotation( annotationType ) != null;
            }
        };
    }

    public static Function<Annotation, Class<? extends Annotation>> type()
    {
        return new Function<Annotation, Class<? extends Annotation>>()
        {
            @Override
            public Class<? extends Annotation> map( Annotation annotation )
            {
                return annotation.annotationType();
            }
        };
    }

    public static Specification<Annotation> isType( final Class<? extends Annotation> annotationType )
    {
        return new Specification<Annotation>()
        {
            @Override
            public boolean satisfiedBy( Annotation annotation )
            {
                return annotation.annotationType().equals( annotationType );
            }
        };
    }

    public static <T extends Annotation> T annotationOn( Type type, Class<T> annotationType )
    {
        return annotationType.cast( Classes.RAW_CLASS.map( type ).getAnnotation( annotationType ) );
    }

    public static Iterable<Annotation> findAccessorAndTypeAnnotationsIn( AccessibleObject accessor )
    {
        return flatten( iterable( accessor.getAnnotations() ),
                        flattenIterables( map( Annotations.ANNOTATIONS_OF, interfacesOf( typeOf( accessor ) ) ) ) );
    }
}
