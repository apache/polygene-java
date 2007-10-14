/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.api.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.annotation.scope.ThisAs;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see MixinModel
 * @see AssertionModel
 */
public abstract class FragmentModel<T>
    extends ObjectModel<T>
{
    private Collection<Class> appliesTo;
    private boolean isAbstract;
    private boolean isGeneric;
    private Set<MethodModel> thisAsMethods;
    private Class declaredBy;

    public FragmentModel( Class<T> fragmentClass, Iterable<ConstructorDependency> constructorDependencies, Iterable<FieldDependency> fieldDependencies, Iterable<MethodDependency> methodDependencies, Class[] appliesTo, Class declaredBy )
    {
        super( fragmentClass, constructorDependencies, fieldDependencies, methodDependencies );
        this.declaredBy = declaredBy;
        this.thisAsMethods = getThisAsMethods( getDependenciesByScope( ThisAs.class ) );

        if( appliesTo == null )
        {
            this.appliesTo = Collections.emptyList();
        }
        else
        {
            this.appliesTo = Arrays.asList( appliesTo );
        }

        isAbstract = getModelClass().getName().indexOf( "EnhancerByCGLIB" ) != -1;
        isGeneric = InvocationHandler.class.isAssignableFrom( getModelClass() );
    }

    public Collection<Class> getAppliesTo()
    {
        return appliesTo;
    }

    public boolean isAbstract()
    {
        return isAbstract;
    }

    public boolean isGeneric()
    {
        return isGeneric;
    }

    public Set<MethodModel> getThisAsMethods()
    {
        return thisAsMethods;
    }

    private Set<MethodModel> getThisAsMethods( Iterable<Dependency> aClass )
    {
        Set<Method> methods = new HashSet<Method>();
        for( Dependency dependency : aClass )
        {
            Class thisAsType = dependency.getKey().getRawType();
            Method[] typeMethods = thisAsType.getMethods();
            methods.addAll( Arrays.asList( typeMethods ) );
        }

        Set<MethodModel> methodModels = new HashSet<MethodModel>();
        for( Method method : methods )
        {
            methodModels.add( new MethodModel( method ) );
        }
        return methodModels;
    }

    public Class getDeclaredBy()
    {
        return declaredBy;
    }
}
