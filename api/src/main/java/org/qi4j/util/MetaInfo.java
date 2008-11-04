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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Used to declare and access metainfo about types.
 */
public final class MetaInfo
{
    private final Map<Class<?>, Object> metaInfoMap;

    public MetaInfo()
    {
        metaInfoMap = new LinkedHashMap<Class<?>, Object>();
    }

    public MetaInfo( MetaInfo metaInfo )
    {
        metaInfoMap = new LinkedHashMap<Class<?>, Object>();
        metaInfoMap.putAll( metaInfo.metaInfoMap );
    }

    public void set( Object metaInfo )
    {
        if( metaInfo instanceof Annotation )
        {
            Annotation annotation = (Annotation) metaInfo;
            metaInfoMap.put( annotation.annotationType(), metaInfo );
        }
        else
        {
            Class<? extends Object> metaInfoclass = metaInfo.getClass();
            Set<Class> types = ClassUtil.classesOf( metaInfoclass );
            for( Type type : types )
            {
                if( type instanceof Class )
                {
                    metaInfoMap.put( (Class) type, metaInfo );
                }
            }
        }
    }

    public <T> T get( Class<T> metaInfoType )
    {
        return (T) metaInfoMap.get( metaInfoType );
    }

    public <T> void add( Class<T> infoType, T info )
    {
        metaInfoMap.put( infoType, info );
    }

    public MetaInfo withAnnotations( AnnotatedElement annotatedElement )
    {
        for( Annotation annotation : annotatedElement.getAnnotations() )
        {
            set( annotation );
        }
        return this;
    }
}
