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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.concern.ConcernsDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.injection.Dependencies;
import org.apache.polygene.runtime.injection.DependencyModel;

/**
 * JAVADOC
 */
public final class ConcernsModel
    implements ConcernsDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    public static final ConcernsModel EMPTY_CONCERNS = new ConcernsModel( Collections.emptyList() );

    private List<ConcernModel> concernsFor;

    public ConcernsModel( List<ConcernModel> concernsFor )
    {
        this.concernsFor = concernsFor;
    }

    @Override
    public Stream<DependencyModel> dependencies()
    {
        return concernsFor.stream().flatMap( ConcernModel::dependencies );
    }

    // Context
    public ConcernsInstance newInstance( Method method, ModuleDescriptor module,
                                         FragmentInvocationHandler mixinInvocationHandler )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        InvocationHandler nextConcern = mixinInvocationHandler;
        for( int i = concernsFor.size() - 1; i >= 0; i-- )
        {
            ConcernModel concernModel = concernsFor.get( i );
            nextConcern = concernModel.newInstance( module, nextConcern, proxyHandler, method );
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
