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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.SideEffects;
import static org.qi4j.util.ClassUtil.*;

/**
 * TODO
 */
public final class SideEffectsDeclaration
{
    private List<SideEffectDeclaration> sideEffectDeclarations = new ArrayList<SideEffectDeclaration>();
    private Map<Method, MethodSideEffectsModel> methodSideEffects = new HashMap<Method, MethodSideEffectsModel>();

    public SideEffectsDeclaration( Class type )
    {
        // Find side-effect declarations
        Set<Type> types;
        if( type.isInterface() )
        {
            types = interfacesOf( type );
        }
        else
        {
            types = Collections.singleton( (Type) type );
        }

        for( Type aType : types )
        {
            addSideEffectDeclaration( aType );
        }
    }

    // Model
    public MethodSideEffectsModel sideEffectsFor( Method method, Class<? extends Composite> compositeType )
    {
        if( !methodSideEffects.containsKey( method ) )
        {
            List<MethodSideEffectModel> sideEffects = new ArrayList<MethodSideEffectModel>();
            for( SideEffectDeclaration sideEffectDeclaration : sideEffectDeclarations )
            {
                if( sideEffectDeclaration.appliesTo( method, compositeType ) )
                {
                    Class sideEffectClass = sideEffectDeclaration.type();
                    sideEffects.add( new MethodSideEffectModel( sideEffectClass ) );
                }
            }

            MethodSideEffectsModel methodConcerns = new MethodSideEffectsModel( method, sideEffects );
            methodSideEffects.put( method, methodConcerns );
            return methodConcerns;
        }
        else
        {
            return methodSideEffects.get( method );
        }
    }

    private void addSideEffectDeclaration( Type type )
    {
        if( type instanceof Class )
        {
            SideEffects annotation = SideEffects.class.cast( ( (Class) type ).getAnnotation( SideEffects.class ) );
            if( annotation != null )
            {
                Class[] sideEffectClasses = annotation.value();
                for( Class sideEffectClass : sideEffectClasses )
                {
                    sideEffectDeclarations.add( new SideEffectDeclaration( sideEffectClass, type ) );
                }
            }
        }
    }
}