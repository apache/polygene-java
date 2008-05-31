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

package org.qi4j.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.injection.InjectionScope;

/**
 * TODO
 */
public class AnnotationUtil
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

    public static boolean isDependencyAnnotation( Annotation annotation )
    {
        return annotation.annotationType().getAnnotation( InjectionScope.class ) != null;
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
}
