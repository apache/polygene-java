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
package org.qi4j.runtime.resolution;

import org.qi4j.api.model.ConcernModel;

/**
 * Modifiers provide stateless modifications of method invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public final class ConcernResolution<T>
    extends ModifierResolution<T>
{
    // Constructors --------------------------------------------------
    public ConcernResolution( ConcernModel<T> concernModel, Iterable<ConstructorDependencyResolution> constructorDependencies, Iterable<FieldDependencyResolution> fieldDependencies, Iterable<MethodDependencyResolution> methodDependencies )
    {
        super( concernModel, constructorDependencies, fieldDependencies, methodDependencies );
    }

    public ConcernModel<T> getConcernModel()
    {
        return (ConcernModel<T>) getObjectModel();
    }
}