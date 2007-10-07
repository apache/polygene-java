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
import java.lang.reflect.Modifier;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see MixinModel
 * @see AssertionModel
 */
public abstract class FragmentModel<T>
    extends ObjectModel<T>
{
    // Attributes ----------------------------------------------------
    protected Class appliesTo;

    // Constructors --------------------------------------------------
    public FragmentModel( Class<T> fragmentClass, Iterable<ConstructorDependency> constructorDependencies, Iterable<FieldDependency> fieldDependencies, Iterable<MethodDependency> methodDependencies, Class appliesTo )
    {
        super( fragmentClass, constructorDependencies, fieldDependencies, methodDependencies );

        this.appliesTo = appliesTo;
    }

    // Public -------------------------------------------------------
    public Class getAppliesTo()
    {
        return appliesTo;
    }

    public boolean isAbstract()
    {
        return Modifier.isAbstract( getModelClass().getModifiers() );
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( getModelClass() );
    }
}
