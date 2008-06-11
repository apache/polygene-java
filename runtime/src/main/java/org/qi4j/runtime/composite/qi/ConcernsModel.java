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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Concerns;
import org.qi4j.runtime.injection.DependencyVisitor;
import static org.qi4j.util.ClassUtil.interfacesOf;

/**
 * TODO
 */
public final class ConcernsModel
{
    private Class<? extends Composite> compositeType;

    private List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
    private Map<Method, MethodConcernsModel> methodConcernsModels = new HashMap<Method, MethodConcernsModel>();

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
    public MethodConcernsModel concernsFor( Method method )
    {
        if( !methodConcernsModels.containsKey( method ) )
        {
            MethodConcernsModel methodConcerns = new MethodConcernsModel( method, compositeType, concerns );
            methodConcernsModels.put( method, methodConcerns );
            return methodConcerns;
        }
        else
        {
            return methodConcernsModels.get( method );
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

    public void visitDependencies( DependencyVisitor visitor )
    {
        for( MethodConcernsModel methodConcernsModel : methodConcernsModels.values() )
        {
            methodConcernsModel.visitDependencies( visitor );
        }
    }
}
