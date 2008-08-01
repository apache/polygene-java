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

package org.qi4j.entity;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.property.AbstractPropertyInstance;
import org.qi4j.property.Property;

/**
 * TODO
 */
public final class LoadingPolicy
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Set<String> loadProperties = new HashSet<String>(); // Qualified names of properties to load
    private String name;
    private boolean recording;

    public LoadingPolicy()
    {
    }

    public LoadingPolicy( String name, boolean recording )
    {
        this.name = name;
        this.recording = recording;
    }

    public Set<String> loadProperties()
    {
        return loadProperties;
    }

    public LoadingPolicy setRecording( boolean isRecording )
    {
        recording = isRecording;
        return this;
    }

    public boolean isRecording()
    {
        return recording;
    }

    public String name()
    {
        return name;
    }

    public LoadingPolicy setName( String name )
    {
        this.name = name;
        return this;
    }

    public LoadingPolicy usesProperty( String qualifiedName )
    {
        loadProperties.add( qualifiedName );
        return this;
    }

    public LoadingPolicy usesMixinType( Class<?> mixinType )
    {
        for( Method mixinMethod : mixinType.getMethods() )
        {
            if( Property.class.isAssignableFrom( mixinMethod.getReturnType() ) )
            {
                ;
            }
            {
                loadProperties.add( AbstractPropertyInstance.getQualifiedName( mixinMethod ) );
            }
        }
        return this;
    }
}
