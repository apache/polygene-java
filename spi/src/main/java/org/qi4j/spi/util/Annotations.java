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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.injection.InjectionScope;
import org.qi4j.api.util.Classes;

/**
 * Useful methods for handling Annotations.
 */
public final class Annotations
{
    public static Annotation getInjectionAnnotation( Annotation[] parameterAnnotation )
    {
        for( Annotation annotation : parameterAnnotation )
        {
            if( isDependencyAnnotation( annotation ) )
            {
                return annotation;
            }
        }
        return null;
    }

    public static <T extends Annotation> T getAnnotationOfType( Annotation[] annotations, Class<T> annotationType )
    {
        for( Annotation annotation : annotations )
        {
            if( annotationType.equals( annotation.annotationType() ) )
            {
                return annotationType.cast( annotation );
            }
        }
        return null;
    }

    public static boolean isDependencyAnnotation( Annotation annotation )
    {
        return annotation.annotationType().getAnnotation( InjectionScope.class ) != null;
    }

    public static boolean isConstraintAnnotation( Annotation annotation )
    {
        return annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null;
    }

    public static boolean isCompositeConstraintAnnotation( Annotation annotation )
    {
        for( Annotation annotation1 : annotation.annotationType().getAnnotations() )
        {
            if( isConstraintAnnotation( annotation1 ) )
            {
                return true;
            }
        }
        return false;
    }

    public static Set<Field> fieldsWithAnnotation( Set<Field> fields, Class<? extends Annotation> annotationType )
    {
        Set<Field> newFields = new HashSet<Field>();
        for( Field field : fields )
        {
            Annotation[] annotations = field.getAnnotations();
            for( Annotation annotation : annotations )
            {
                if( isOfAnnotationType( annotation, annotationType ) )
                {
                    newFields.add( field );
                    break;
                }
            }
        }

        return newFields;
    }

    private static boolean isOfAnnotationType( Annotation annotation, Class<? extends Annotation> annotationType )
    {
        if( annotationType.equals( annotation.getClass() ) )
        {
            return true;
        }

        // Check annotations of this annotation
        for( Annotation annotation1 : annotation.annotationType().getAnnotations() )
        {
            if( isOfAnnotationType( annotation1, annotationType ) )
            {
                return true;
            }
        }

        return false;
    }

    public static <T extends Annotation> T getAnnotation( Type type, Class<T> annotationType )
    {
        if( !( type instanceof Class ) )
        {
            return null;
        }
        return annotationType.cast( ( (Class<?>) type ).getAnnotation( annotationType ) );
    }

    public static Annotation[] getMethodAndTypeAnnotations( Method method )
    {
        List<Annotation> annotationList = new ArrayList<Annotation>( Arrays.asList( method.getAnnotations() ) );
        Set<Class> interfaces = Classes.interfacesOf( method.getReturnType() );
        for( Class anInterface : interfaces )
        {
            annotationList.addAll( Arrays.asList( anInterface.getAnnotations() ) );
        }
        Annotation[] annotations = annotationList.toArray( new Annotation[annotationList.size()] );

        return annotations;
    }
}
