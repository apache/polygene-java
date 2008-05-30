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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TODO
 */
public final class InjectedMethodModel
{
    // Model
    private Method method;
    private InjectedParametersModel parameters;

    public InjectedMethodModel( Method method, InjectedParametersModel parameters )
    {
        this.method = method;
        this.parameters = parameters;
    }

    // Binding
    public void bind( Resolution resolution )
    {
        parameters.bind( resolution );
    }

    // Context
    public void inject( InjectionContext context, Object instance ) throws InjectionException
    {
        Object[] params = parameters.newParametersInstance( context );
        try
        {
            method.invoke( instance, params );
        }
        catch( IllegalAccessException e )
        {
            throw new InjectionException( e );
        }
        catch( InvocationTargetException e )
        {
            throw new InjectionException( e.getTargetException() );
        }
    }

    public void visitDependencies( DependencyVisitor visitor )
    {
        parameters.visitDependencies( visitor );
    }
}