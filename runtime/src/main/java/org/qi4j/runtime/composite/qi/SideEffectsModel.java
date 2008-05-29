/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.SideEffects;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.SideEffectInvocationHandlerResult;
import org.qi4j.runtime.composite.TypedFragmentInvocationHandler;
import org.qi4j.runtime.structure.qi.ModuleInstance;
import static org.qi4j.util.ClassUtil.interfacesOf;

/**
 * TODO
 */
public class SideEffectsModel
{
    private Class<? extends Composite> compositeType;

    private List<SideEffectDeclaration> sideEffectDeclarations = new ArrayList<SideEffectDeclaration>();
    private Map<Method, List<SideEffectModel>> methodSideEffects = new HashMap<Method, List<SideEffectModel>>();

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
    public void sideEffectsFor( Method method )
    {
        if( !methodSideEffects.containsKey( method ) )
        {
            List<SideEffectModel> sideEffectsForMethod = new ArrayList<SideEffectModel>();
            for( SideEffectDeclaration sideEffectDeclaration : sideEffectDeclarations )
            {
                if( sideEffectDeclaration.appliesTo( method, compositeType ) )
                {
                    Class sideEffectClass = sideEffectDeclaration.type();
                    sideEffectsForMethod.add( new SideEffectModel( sideEffectClass ) );
                }
            }
            methodSideEffects.put( method, sideEffectsForMethod );
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

    // Binding
    public void bind( BindingContext bindingContext )
    {
        for( List<SideEffectModel> sideEffectModels : methodSideEffects.values() )
        {
            for( SideEffectModel sideEffectModel : sideEffectModels )
            {
                sideEffectModel.bind( bindingContext );
            }
        }
    }

    // Context
    public SideEffectsInstance newInstance( ModuleInstance moduleInstance, Method method )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        List<SideEffectModel> sideEffectModels = methodSideEffects.get( method );
        SideEffectInvocationHandlerResult result = new SideEffectInvocationHandlerResult();
        List<InvocationHandler> sideEffects = new ArrayList<InvocationHandler>( sideEffectModels.size() );
        for( SideEffectModel sideEffectModel : sideEffectModels )
        {
            Object sideEffect = sideEffectModel.newInstance( moduleInstance, result, proxyHandler );
            if( sideEffectModel.isGeneric() )
            {
                sideEffects.add( (InvocationHandler) sideEffect );
            }
            else
            {
                sideEffects.add( new TypedFragmentInvocationHandler( sideEffect ) );
            }
        }
        return new SideEffectsInstance( method, sideEffects, result, proxyHandler );
    }

}