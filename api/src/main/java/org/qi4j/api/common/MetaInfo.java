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

package org.qi4j.api.common;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.util.Classes;

import static java.util.Arrays.*;

/**
 * Used to declare and access meta-info.
 */
public final class MetaInfo
    implements Serializable
{
    private final static Collection<Class> ignored;

    static
    {
        ignored = new HashSet<Class>( 4, 0.8f ); // Optimize size used.
        ignored.addAll( asList( Mixins.class, Concerns.class, SideEffects.class ) );
    }

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
            Class<?> metaInfoclass = metaInfo.getClass();
            Set<Class> types = Classes.typesOf( metaInfoclass );
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
        return metaInfoType.cast( metaInfoMap.get( metaInfoType ) );
    }

    public <T> void add( Class<T> infoType, T info )
    {
        metaInfoMap.put( infoType, info );
    }

    public MetaInfo withAnnotations( AnnotatedElement annotatedElement )
    {
        for( Annotation annotation : annotatedElement.getAnnotations() )
        {
            if( !ignored.contains( annotation.annotationType() ) )
            {
                set( annotation );
            }
        }
        return this;
    }

    @Override
    public String toString()
    {
        return metaInfoMap.toString();
    }

    public void remove( Class serviceFinderClass )
    {
        metaInfoMap.remove( serviceFinderClass );
    }
}
