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

package org.qi4j.spi.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.qi4j.api.util.Classes;

/**
 * Useful methods for handling Annotations.
 */
public final class Annotations
{
    public static <A extends Annotation> Iterable<A> filter(AnnotationSpecification specification, Iterable<? extends Annotation> iterable)
    {
        return (Iterable<A>) new FilterIterable(iterable, specification);
    }

    public static <A extends Annotation> A first(Iterable<? extends Annotation> iterable)
    {
        Iterator<? extends Annotation> iter = iterable.iterator();
        if (iter.hasNext())
            return (A) iter.next();
        else
            return null;
    }

    public static <A extends Annotation> A first(AnnotationSpecification specification, Iterable<? extends Annotation> iterable)
    {
        for( Annotation annotation : iterable )
        {
            if (specification.valid( annotation ))
                return (A) annotation;
        }

        return null;
    }

    public static <A extends Annotation> A first(AnnotationSpecification specification, Annotation... annotations)
    {
        for( Annotation annotation : annotations )
        {
            if (specification.valid( annotation ))
                return (A) annotation;
        }

        return null;
    }

    public static AnnotationSpecification hasAnnotation( final Class<? extends Annotation> annotationType)
    {
        return new AnnotationSpecification()
        {
            public boolean valid( Annotation annotation )
            {
                return annotation.annotationType().getAnnotation( annotationType ) != null;
            }
        };
    }

    public static boolean hasValid(AnnotationSpecification specification, Iterable<? extends Annotation> annotations)
    {
        for( Annotation annotation : annotations )
        {
            if (specification.valid( annotation ))
                return true;
        }
        return false;
    }

    public static AnnotationSpecification isType( final Class<? extends Annotation> annotationType)
    {
        return new AnnotationSpecification()
        {
            public boolean valid( Annotation annotation )
            {
                return annotation.annotationType().equals(annotationType);
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
        List<Annotation> annotationList = new ArrayList<Annotation>( Arrays.asList( method.getAnnotations() ) );
        Set<Class> interfaces = Classes.interfacesOf( method.getReturnType() );
        for( Class anInterface : interfaces )
        {
            annotationList.addAll( Arrays.asList( anInterface.getAnnotations() ) );
        }
        Annotation[] annotations = annotationList.toArray( new Annotation[annotationList.size()] );

        return Arrays.asList( annotations);
    }

    public interface AnnotationSpecification
    {
        boolean valid( Annotation annotation );
    }

    public static class FilterIterable
        implements Iterable<Annotation>
    {
        private Iterable<? extends Annotation> iterable;
        private AnnotationSpecification specification;

        public FilterIterable( Iterable<? extends Annotation> iterable, AnnotationSpecification specification )
        {
            this.iterable = iterable;
            this.specification = specification;
        }

        public Iterator<Annotation> iterator()
        {
            return new FilterIterator( iterable.iterator(), specification );
        }

        static class FilterIterator
            implements Iterator<Annotation>
        {
            private Iterator<? extends Annotation> iterator;
            private AnnotationSpecification specification;
            private Annotation currentValue;
            boolean finished = false;
            boolean nextConsumed = true;

            public FilterIterator( Iterator<? extends Annotation> iterator, AnnotationSpecification specification )
            {
                this.specification = specification;
                this.iterator = iterator;
            }

            public boolean moveToNextValid()
            {
                boolean found = false;
                while( !found && iterator.hasNext() )
                {
                    Annotation currentValue = iterator.next();
                    if( specification.valid( ( currentValue ) ))
                    {
                        found = true;
                        this.currentValue = currentValue;
                        nextConsumed = false;
                    }
                }
                if( !found )
                {
                    finished = true;
                }
                return found;
            }

            public Annotation next()
            {
                if( !nextConsumed )
                {
                    nextConsumed = true;
                    return currentValue;
                }
                else
                {
                    if( !finished )
                    {
                        if( moveToNextValid() )
                        {
                            nextConsumed = true;
                            return currentValue;
                        }
                    }
                }
                return null;
            }

            public boolean hasNext()
            {
                return !finished &&
                       ( !nextConsumed || moveToNextValid() );
            }

            public void remove()
            {
            }
        }
    }
}
