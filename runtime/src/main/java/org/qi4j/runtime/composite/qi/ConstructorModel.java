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

import java.lang.reflect.Constructor;
import org.qi4j.composite.InstantiationException;

/**
 * TODO
 */
public final class ConstructorModel
{
    private Constructor constructor;

    private InjectedParametersModel parameters;

    public ConstructorModel( Constructor constructor, InjectedParametersModel parameters )
    {
        constructor.setAccessible( true );
        this.constructor = constructor;
        this.parameters = parameters;
    }

    public void visitDependencies( DependencyVisitor dependencyVisitor )
    {
        parameters.visitDependencies( dependencyVisitor );
    }

    // Binding
    public void bind( BindingContext context )
    {
        parameters.bind( context );
    }

    // Context
    public Object newInstance( InjectionContext context )
        throws InstantiationException
    {
        // Create parameters
        Object[] parametersInstance = parameters.newInstance( context );

        // Invoke constructor
        try
        {
            Object instance = constructor.newInstance( parametersInstance );
            return instance;
        }
        catch( Exception e )
        {
            throw new InstantiationException( "Could not instantiate " + constructor.getDeclaringClass(), e );
        }
    }
}
