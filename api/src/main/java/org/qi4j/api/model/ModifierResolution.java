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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import org.qi4j.api.ConstructorDependencyResolution;
import org.qi4j.api.DependencyResolution;
import org.qi4j.api.FieldDependencyResolution;
import org.qi4j.api.MethodDependencyResolution;
import org.qi4j.api.annotation.Modifies;

/**
 * Modifiers provide stateless modifications of method invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public final class ModifierResolution<T>
    extends FragmentResolution<T>
{
    // Constructors --------------------------------------------------
    public ModifierResolution( ModifierModel<T> modifierModel, Iterable<ConstructorDependencyResolution> constructorDependencies, Iterable<FieldDependencyResolution> fieldDependencies, Iterable<MethodDependencyResolution> methodDependencies)
    {
        super( modifierModel, constructorDependencies, fieldDependencies, methodDependencies);
    }
}