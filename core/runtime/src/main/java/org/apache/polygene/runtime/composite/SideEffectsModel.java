/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.sideeffect.SideEffectsDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.injection.Dependencies;
import org.apache.polygene.runtime.injection.DependencyModel;

/**
 * JAVADOC
 */
public final class SideEffectsModel
    implements SideEffectsDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    public static final SideEffectsModel EMPTY_SIDEEFFECTS = new SideEffectsModel( Collections.emptyList() );

    private List<SideEffectModel> sideEffectModels = null;

    public SideEffectsModel( List<SideEffectModel> sideEffectModels )
    {
        this.sideEffectModels = sideEffectModels;
    }

    @Override
    public Stream<DependencyModel> dependencies()
    {
        return sideEffectModels.stream().flatMap( Dependencies::dependencies );
    }

    // Context
    public SideEffectsInstance newInstance( Method method, ModuleDescriptor module, InvocationHandler invoker )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        SideEffectInvocationHandlerResult result = new SideEffectInvocationHandlerResult();
        List<InvocationHandler> sideEffects = new ArrayList<>( sideEffectModels.size() );
        for( SideEffectModel sideEffectModel : sideEffectModels )
        {
            InvocationHandler sideEffect = sideEffectModel.newInstance( module, result, proxyHandler, method );
            sideEffects.add( sideEffect );
        }
        return new SideEffectsInstance( sideEffects, result, proxyHandler, invoker );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            for( SideEffectModel sideEffectModel : sideEffectModels )
            {
                if( !sideEffectModel.accept( modelVisitor ) )
                {
                    break;
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }
}