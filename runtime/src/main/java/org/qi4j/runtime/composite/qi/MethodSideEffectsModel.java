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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.SideEffectInvocationHandlerResult;
import org.qi4j.runtime.composite.TypedFragmentInvocationHandler;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * TODO
 */
public class MethodSideEffectsModel
{
    private List<MethodSideEffectModel> sideEffectsForMethod;

    public MethodSideEffectsModel( Method method, Class<? extends Composite> compositeType, List<SideEffectDeclaration> sideEffectDeclarations )
    {
        sideEffectsForMethod = new ArrayList<MethodSideEffectModel>();
        for( SideEffectDeclaration sideEffectDeclaration : sideEffectDeclarations )
        {
            if( sideEffectDeclaration.appliesTo( method, compositeType ) )
            {
                Class sideEffectClass = sideEffectDeclaration.type();
                sideEffectsForMethod.add( new MethodSideEffectModel( sideEffectClass ) );
            }
        }
    }

    // Binding
    public void bind( Resolution resolution )
    {
        for( MethodSideEffectModel methodSideEffectModel : sideEffectsForMethod )
        {
            methodSideEffectModel.bind( resolution );
        }
    }

    // Context
    public MethodSideEffectsInstance newInstance( ModuleInstance moduleInstance, Method method )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        SideEffectInvocationHandlerResult result = new SideEffectInvocationHandlerResult();
        List<InvocationHandler> sideEffects = new ArrayList<InvocationHandler>( sideEffectsForMethod.size() );
        for( MethodSideEffectModel sideEffectModel : sideEffectsForMethod )
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
        return new MethodSideEffectsInstance( method, sideEffects, result, proxyHandler );
    }

    private static final class MethodSideEffectModel
        extends AbstractModifierModel
    {
        private MethodSideEffectModel( Class sideEffectClass )
        {
            super( sideEffectClass );
        }

    }

}