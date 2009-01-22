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
import java.util.ArrayList;
import java.util.List;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.concern.MethodConcernsDescriptor;

/**
 * TODO
 */
public final class MethodConcernsModel
    implements Binder, MethodConcernsDescriptor
{
    private final List<MethodConcernModel> concernsForMethod;
    private final Method method;

    public MethodConcernsModel( Method method, List<MethodConcernModel> concernsForMethod )
    {
        this.method = method;
        this.concernsForMethod = concernsForMethod;
    }

    public Method method()
    {
        return method;
    }

    public boolean hasConcerns()
    {
        return !concernsForMethod.isEmpty();
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        for( MethodConcernModel concernModel : concernsForMethod )
        {
            concernModel.bind( resolution );
        }
    }

    // Context
    public MethodConcernsInstance newInstance( ModuleInstance moduleInstance, FragmentInvocationHandler mixinInvocationHandler )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        Object nextConcern = mixinInvocationHandler;
        for( int i = concernsForMethod.size() - 1; i >= 0; i-- )
        {
            MethodConcernModel concernModel = concernsForMethod.get( i );

            nextConcern = concernModel.newInstance( moduleInstance, nextConcern, proxyHandler );
        }

        InvocationHandler firstConcern;
        if( nextConcern instanceof InvocationHandler )
        {
            firstConcern = (InvocationHandler) nextConcern;
        }
        else
        {
            firstConcern = new TypedFragmentInvocationHandler( nextConcern );
        }

        return new MethodConcernsInstance( firstConcern, mixinInvocationHandler, proxyHandler );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
        for( MethodConcernModel methodConcernModel : concernsForMethod )
        {
            methodConcernModel.visitModel( modelVisitor );
        }
    }

    public MethodConcernsModel combineWith( MethodConcernsModel mixinMethodConcernsModel )
    {
        List<MethodConcernModel> combinedModels = new ArrayList<MethodConcernModel>( concernsForMethod.size() + mixinMethodConcernsModel.concernsForMethod.size() );
        combinedModels.addAll( concernsForMethod );
        combinedModels.addAll( mixinMethodConcernsModel.concernsForMethod );
        return new MethodConcernsModel( method, combinedModels );
    }
}
