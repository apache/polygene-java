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

package org.qi4j.api.usecase;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;

/**
 * JAVADOC
 */
public final class StateUsage
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Set<String> usedEntityTypes = new HashSet<String>();
    private Set<String> usedMixinTypes = new HashSet<String>();
    private Set<String> usedProperties = new HashSet<String>(); // Qualified names of properties that are used
    private boolean recording;

    public StateUsage()
    {
    }

    public StateUsage( boolean recording )
    {
        this.recording = recording;
    }

    public Set<String> usedProperties()
    {
        return usedProperties;
    }

    public StateUsage setRecording( boolean isRecording )
    {
        recording = isRecording;
        return this;
    }

    public boolean isRecording()
    {
        return recording;
    }

    public StateUsage usesEntityType( Class entityType )
    {
        usedEntityTypes.add( entityType.getName() );
        return this;
    }

    public StateUsage usesMixinType( Class<?> mixinType )
    {
        for( Method mixinMethod : mixinType.getMethods() )
        {
            if( Property.class.isAssignableFrom( mixinMethod.getReturnType() ) )
            {
                String qualifiedName = GenericPropertyInfo.getQualifiedName( mixinMethod );
                usedProperties.add( qualifiedName );
                usedMixinTypes.add( GenericPropertyInfo.getDeclaringClassName( qualifiedName ) );
            }
        }
        return this;
    }

    public StateUsage usesProperty( String qualifiedName )
    {
        usedProperties.add( qualifiedName );
        usedMixinTypes.add( GenericPropertyInfo.getDeclaringClassName( qualifiedName ) );
        return this;
    }

    @Override public String toString()
    {
        return usedProperties.toString() + ( recording ? "(recording)" : "" );
    }
}
