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
import org.qi4j.composite.Concerns;
import org.qi4j.runtime.composite.FragmentInvocationHandler;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.TypedFragmentInvocationHandler;
import org.qi4j.runtime.structure.qi.ModuleInstance;
import static org.qi4j.util.ClassUtil.interfacesOf;

/**
 * TODO
 */
public class ConcernsModel
{
    private Class<? extends Composite> compositeType;

    private List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
    private Map<Method, List<ConcernModel>> methodConcerns = new HashMap<Method, List<ConcernModel>>();

    public ConcernsModel( Class<? extends Composite> compositeType )
    {
        this.compositeType = compositeType;

        // Find concern declarations
        Set<Type> interfaces = interfacesOf( compositeType );

        for( Type anInterface : interfaces )
        {
            addConcernDeclarations( anInterface );
        }
    }

    // Model
    public void concernsFor( Method method )
    {
        if( !methodConcerns.containsKey( method ) )
        {
            List<ConcernModel> concernsForMethod = new ArrayList<ConcernModel>();
            for( ConcernDeclaration concern : concerns )
            {
                if( concern.appliesTo( method, compositeType ) )
                {
                    Class concernClass = concern.type();
                    concernsForMethod.add( new ConcernModel( concernClass ) );
                }
            }
            methodConcerns.put( method, concernsForMethod );
        }
    }

    private void addConcernDeclarations( Type type )
    {
        if( type instanceof Class )
        {
            Concerns annotation = Concerns.class.cast( ( (Class) type ).getAnnotation( Concerns.class ) );
            if( annotation != null )
            {
                Class[] concernClasses = annotation.value();
                for( Class concernClass : concernClasses )
                {
                    concerns.add( new ConcernDeclaration( concernClass, type ) );
                }
            }
        }
    }

    // Binding
    public void bind( BindingContext bindingContext )
    {
        for( List<ConcernModel> concernModels : methodConcerns.values() )
        {
            for( ConcernModel concernModel : concernModels )
            {
                concernModel.bind( bindingContext );
            }
        }
    }

    // Context
    public ConcernsInstance newInstance( ModuleInstance moduleInstance, Method method, FragmentInvocationHandler mixinInvocationHandler )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        List<ConcernModel> concernModels = methodConcerns.get( method );
        Object nextConcern = mixinInvocationHandler;
        for( int i = concernModels.size() - 1; i >= 0; i-- )
        {
            ConcernModel concernModel = concernModels.get( i );

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

        return new ConcernsInstance( method, firstConcern, mixinInvocationHandler, proxyHandler );
    }

}
