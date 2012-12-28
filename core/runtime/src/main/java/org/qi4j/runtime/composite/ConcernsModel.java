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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.qi4j.api.concern.ConcernsDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * JAVADOC
 */
public final class ConcernsModel
    implements ConcernsDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    public static final ConcernsModel EMPTY_CONCERNS = new ConcernsModel( Collections.<ConcernModel>emptyList() );

    private List<ConcernModel> concernsFor;

    public ConcernsModel( List<ConcernModel> concernsFor )
    {
        this.concernsFor = concernsFor;
    }

    @Override
    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flattenIterables( Iterables.map( Dependencies.DEPENDENCIES_FUNCTION, concernsFor ) );
    }

    // Context
    public ConcernsInstance newInstance( Method method, ModuleInstance moduleInstance,
                                         FragmentInvocationHandler mixinInvocationHandler
    )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        InvocationHandler nextConcern = mixinInvocationHandler;
        for( int i = concernsFor.size() - 1; i >= 0; i-- )
        {
            ConcernModel concernModel = concernsFor.get( i );

            nextConcern = concernModel.newInstance( moduleInstance, nextConcern, proxyHandler, method );
        }

        return new ConcernsInstance( nextConcern, mixinInvocationHandler, proxyHandler );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            for( ConcernModel concernModel : concernsFor )
            {
                if( !concernModel.accept( modelVisitor ) )
                {
                    break;
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }
}
