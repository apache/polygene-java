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

/**
 * TODO
 */
public class InjectedParameterModel
{
    // Model
    private ParameterModel parameterModel;
    private DependencyModel dependencyModel;

    public InjectedParameterModel( ParameterModel parameterModel, DependencyModel dependencyModel )
    {
        this.parameterModel = parameterModel;
        this.dependencyModel = dependencyModel;
    }

    public DependencyModel dependency()
    {
        return dependencyModel;
    }

    public void visitDependency( DependencyVisitor dependencyVisitor )
    {
        dependencyVisitor.visit( dependencyModel );
    }

    // Binding
    public void bind( BindingContext context )
    {
        dependencyModel.bind( context );
    }

    // Context
    public Object inject( InjectionContext context )
    {
        return dependencyModel.inject( context );
    }

}
