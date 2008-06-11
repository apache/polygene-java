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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.SideEffects;
import org.qi4j.runtime.injection.DependencyVisitor;
import static org.qi4j.util.ClassUtil.interfacesOf;

/**
 * TODO
 */
public final class SideEffectsModel
{
    private Class<? extends Composite> compositeType;

    private List<SideEffectDeclaration> sideEffectDeclarations = new ArrayList<SideEffectDeclaration>();
    private Map<Method, MethodSideEffectsModel> methodSideEffects = new HashMap<Method, MethodSideEffectsModel>();

    public SideEffectsModel( Class<? extends Composite> compositeType )
    {
        this.compositeType = compositeType;

        // Find side-effect declarations
        Set<Type> interfaces = interfacesOf( compositeType );

        for( Type anInterface : interfaces )
        {
            addSideEffectDeclaration( anInterface );
        }
    }

    // Model
    public MethodSideEffectsModel sideEffectsFor( Method method )
    {
        if( !methodSideEffects.containsKey( method ) )
        {
            MethodSideEffectsModel methodConcerns = new MethodSideEffectsModel( method, compositeType, sideEffectDeclarations );
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

    public void visitDependencies( DependencyVisitor visitor )
    {
        for( MethodSideEffectsModel methodSideEffectsModel : methodSideEffects.values() )
        {
            methodSideEffectsModel.visitDependencies( visitor );
        }
    }
}