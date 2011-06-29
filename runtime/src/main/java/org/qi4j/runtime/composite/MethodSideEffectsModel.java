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

import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.util.VisitableHierarchy;
import org.qi4j.runtime.bootstrap.AssemblyHelper;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.sideeffect.MethodSideEffectsDescriptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JAVADOC
 */
public final class MethodSideEffectsModel
    implements MethodSideEffectsDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    private Method method;
    private List<MethodSideEffectModel> sideEffectModels = null;

    public MethodSideEffectsModel( Method method, List<MethodSideEffectModel> sideEffectModels )
    {
        this.method = method;
        this.sideEffectModels = sideEffectModels;
    }

    public boolean hasSideEffects()
    {
        return !sideEffectModels.isEmpty();
    }

    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flattenIterables( Iterables.map( Dependencies.DEPENDENCIES_FUNCTION, sideEffectModels ) );
    }

    // Context
    public MethodSideEffectsInstance newInstance( ModuleInstance moduleInstance, InvocationHandler invoker )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        SideEffectInvocationHandlerResult result = new SideEffectInvocationHandlerResult();
        List<InvocationHandler> sideEffects = new ArrayList<InvocationHandler>( sideEffectModels.size() );
        for( MethodSideEffectModel sideEffectModel : sideEffectModels )
        {
            InvocationHandler sideEffect = sideEffectModel.newInstance( moduleInstance, result, proxyHandler, method );
            sideEffects.add( sideEffect );
        }
        return new MethodSideEffectsInstance( sideEffects, result, proxyHandler, invoker );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor ) throws ThrowableType
    {
        if (modelVisitor.visitEnter( this ))
        {
            for( MethodSideEffectModel methodSideEffectModel : sideEffectModels )
            {
                if (!methodSideEffectModel.accept( modelVisitor ))
                    break;
            }
        }
        return modelVisitor.visitLeave( this );
    }

    public MethodSideEffectsModel combineWith( MethodSideEffectsModel mixinMethodSideEffectsModel )
    {
        if( mixinMethodSideEffectsModel.sideEffectModels.size() > 0 )
        {
            List<MethodSideEffectModel> combinedModels = new ArrayList<MethodSideEffectModel>( sideEffectModels.size() + mixinMethodSideEffectsModel
                .sideEffectModels
                .size() );
            combinedModels.addAll( sideEffectModels );
            combinedModels.removeAll( mixinMethodSideEffectsModel.sideEffectModels );
            combinedModels.addAll( mixinMethodSideEffectsModel.sideEffectModels );
            return new MethodSideEffectsModel( method, combinedModels );
        }
        else
        {
            return this;
        }
    }

    static MethodSideEffectsModel createForMethod( Method method,
                                                   Collection<Class> sideEffectClasses,
                                                   AssemblyHelper helper
    )
    {
        List<MethodSideEffectModel> sideEffects = new ArrayList<MethodSideEffectModel>();
        for( Class sideEffectClass : sideEffectClasses )
        {
            sideEffects.add( helper.getSideEffectModel( sideEffectClass ) );
        }

        return new MethodSideEffectsModel( method, sideEffects );
    }
}