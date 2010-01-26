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

package org.qi4j.runtime.composite;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.spi.util.Annotations;
import org.qi4j.spi.util.MethodKeyMap;

import static java.util.Collections.*;
import static org.qi4j.api.util.Classes.*;

/**
 * JAVADOC
 */
public final class SideEffectsDeclaration
    implements Serializable
{
    private final List<SideEffectDeclaration> sideEffectDeclarations = new ArrayList<SideEffectDeclaration>();
    private final Map<Method, MethodSideEffectsModel> methodSideEffects = new MethodKeyMap<MethodSideEffectsModel>();

    public SideEffectsDeclaration( Class type, Iterable<Class<?>> sideEffects )
    {
        Collection<Type> types = asSideEffectsTargetTypes( type );

        for( Type aType : types )
        {
            addSideEffectDeclaration( aType );
        }

        // Add sideeffects from assembly
        for( Class<?> sideEffect : sideEffects )
        {
            this.sideEffectDeclarations.add( new SideEffectDeclaration( sideEffect, null ) );
        }
    }

    private Collection<Type> asSideEffectsTargetTypes( Class type )
    {
        // Find side-effect declarations
        if( type.isInterface() )
        {
            return genericInterfacesOf( type );
        }
        else
        {
            return singleton( (Type) type );
        }
    }

    // Model

    public MethodSideEffectsModel sideEffectsFor( Method method, Class<? extends Composite> compositeType )
    {
        if( methodSideEffects.containsKey( method ) )
        {
            return methodSideEffects.get( method );
        }

        final Collection<Class> matchingSideEffects = matchingSideEffectClasses( method, compositeType );
        MethodSideEffectsModel methodConcerns = MethodSideEffectsModel.createForMethod( method, matchingSideEffects );
        methodSideEffects.put( method, methodConcerns );
        return methodConcerns;
    }

    private Collection<Class> matchingSideEffectClasses( Method method, Class<? extends Composite> compositeType )
    {
        Collection<Class> result = new LinkedHashSet<Class>( sideEffectDeclarations.size() );

        for( SideEffectDeclaration sideEffectDeclaration : sideEffectDeclarations )
        {
            if( sideEffectDeclaration.appliesTo( method, compositeType ) )
            {
                result.add( sideEffectDeclaration.type() );
            }
        }
        return result;
    }

    private void addSideEffectDeclaration( Type type )
    {
        if( type instanceof Class )
        {
            final Class clazz = (Class) type;
            SideEffects annotation = Annotations.getAnnotation( type, SideEffects.class );
            if( annotation != null )
            {
                Class[] sideEffectClasses = annotation.value();
                for( Class sideEffectClass : sideEffectClasses )
                {
                    sideEffectDeclarations.add( new SideEffectDeclaration( sideEffectClass, clazz ) );
                }
            }
        }
    }
}