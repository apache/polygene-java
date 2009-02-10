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

package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.io.Serializable;

/**
 * Custom implementation of AnnotatedElement. This is primarily used
 * to aggregate annotations in interface and mixin methods.
 */
public final class MapAnnotatedElement
    implements AnnotatedElement, Serializable
{
    final Map<Class<? extends Annotation>, Annotation> annotations;

    public MapAnnotatedElement( Map<Class<? extends Annotation>, Annotation> annotations )
    {
        this.annotations = annotations;
    }

    public boolean isAnnotationPresent( Class<? extends Annotation> aClass )
    {
        return annotations.get( aClass ) != null;
    }

    public <T extends Annotation> T getAnnotation( Class<T> aClass )
    {
        return aClass.cast( annotations.get( aClass ) );
    }

    public Annotation[] getAnnotations()
    {
        return annotations.values().toArray( new Annotation[annotations.size()] );
    }

    public Annotation[] getDeclaredAnnotations()
    {
        return getAnnotations();
    }
}
