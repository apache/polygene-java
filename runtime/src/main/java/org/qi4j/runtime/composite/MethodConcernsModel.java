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

import org.qi4j.api.concern.MethodConcernsDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public final class MethodConcernsModel
    implements MethodConcernsDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    private List<MethodConcernModel> concernsForMethod;
    private Method method;

    public MethodConcernsModel( Method method, List<MethodConcernModel> concernsForMethod )
    {
        this.method = method;
        this.concernsForMethod = concernsForMethod;
    }

    public boolean hasConcerns()
    {
        return !concernsForMethod.isEmpty();
    }

    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flattenIterables( Iterables.map( Dependencies.DEPENDENCIES_FUNCTION, concernsForMethod ) );
    }

    // Context
    public MethodConcernsInstance newInstance( ModuleInstance moduleInstance,
                                               FragmentInvocationHandler mixinInvocationHandler
    )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        InvocationHandler nextConcern = mixinInvocationHandler;
        for( int i = concernsForMethod.size() - 1; i >= 0; i-- )
        {
            MethodConcernModel concernModel = concernsForMethod.get( i );

            nextConcern = concernModel.newInstance( moduleInstance, nextConcern, proxyHandler, method );
        }

        return new MethodConcernsInstance( nextConcern, mixinInvocationHandler, proxyHandler );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor ) throws ThrowableType
    {
        if (modelVisitor.visitEnter( this ))
        {
            for( MethodConcernModel methodConcernModel : concernsForMethod )
            {
                if (!methodConcernModel.accept( modelVisitor ))
                    break;
            }
        }
        return modelVisitor.visitLeave( this );
    }

    public MethodConcernsModel combineWith( MethodConcernsModel mixinMethodConcernsModel )
    {
        if( mixinMethodConcernsModel == null )
        {
            return this;
        }
        else if( mixinMethodConcernsModel.concernsForMethod.size() > 0 )
        {
            List<MethodConcernModel> combinedModels = new ArrayList<MethodConcernModel>( concernsForMethod.size() + mixinMethodConcernsModel
                .concernsForMethod
                .size() );
            combinedModels.addAll( concernsForMethod );
            combinedModels.removeAll( mixinMethodConcernsModel.concernsForMethod ); // Remove duplicates
            combinedModels.addAll( mixinMethodConcernsModel.concernsForMethod );
            return new MethodConcernsModel( method, combinedModels );
        }
        else
        {
            return this;
        }
    }
}
