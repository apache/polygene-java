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
import org.qi4j.composite.Composite;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * TODO
 */
public final class MethodConcernsModel
    implements Binder
{
    private List<MethodConcernModel> concernsForMethod;
    private Method method;

    public MethodConcernsModel( Method method, Class<? extends Composite> compositeType, List<ConcernDeclaration> concerns )
    {
        this.method = method;

        concernsForMethod = new ArrayList<MethodConcernModel>();
        for( ConcernDeclaration concern : concerns )
        {
            if( concern.appliesTo( method, compositeType ) )
            {
                Class concernClass = concern.type();
                concernsForMethod.add( new MethodConcernModel( concernClass ) );
            }
        }
    }

    public Method method()
    {
        return method;
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
    public MethodConcernsInstance newInstance( ModuleInstance moduleInstance, Method method, FragmentInvocationHandler mixinInvocationHandler )
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

        return new MethodConcernsInstance( method, firstConcern, mixinInvocationHandler, proxyHandler );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( MethodConcernModel methodConcernModel : concernsForMethod )
        {
            methodConcernModel.visitModel( modelVisitor );
        }
    }
}
