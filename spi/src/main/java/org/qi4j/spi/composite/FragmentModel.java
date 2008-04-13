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
package org.qi4j.spi.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.composite.scope.This;
import org.qi4j.spi.injection.InjectionModel;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see MixinModel
 * @see ConcernModel
 */
public abstract class FragmentModel extends AbstractModel
{
    private Collection<Class> appliesTo;
    private boolean isAbstract;
    private boolean isGeneric;
    private Set<Method> thisAsMethods;

    protected FragmentModel( Class fragmentClass, Iterable<ConstructorModel> constructorModels, Iterable<FieldModel> fieldModels, Iterable<MethodModel> methodDependencies, Class[] appliesTo )
    {
        super( fragmentClass, constructorModels, fieldModels, methodDependencies );
        this.thisAsMethods = getThisMethods( getInjectionsByScope( This.class ) );

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

    public Set<Method> getThisMethods()
    {
        return thisAsMethods;
    }

    private Set<Method> getThisMethods( Iterable<InjectionModel> aClass )
    {
        Set<Method> methods = new HashSet<Method>();
        for( InjectionModel injectionModel : aClass )
        {
            Class thisAsType = injectionModel.getRawInjectionType();
            Method[] typeMethods = thisAsType.getMethods();
            methods.addAll( Arrays.asList( typeMethods ) );
        }

        return methods;
    }
}
