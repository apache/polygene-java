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

import java.lang.reflect.Field;

/**
 * TODO
 */
public final class InjectedFieldModel
{
    private DependencyModel dependencyModel;
    private Field injectedField;

    public InjectedFieldModel( Field injectedField, DependencyModel dependencyModel )
    {
        injectedField.setAccessible( true );
        this.injectedField = injectedField;
        this.dependencyModel = dependencyModel;
    }

    public void bind( Resolution context )
    {
        dependencyModel.bind( context );
    }

    public void inject( InjectionContext context, Object instance )
    {
        Object value = dependencyModel.inject( context );
        try
        {
            injectedField.set( instance, value );
        }
        catch( IllegalAccessException e )
        {
            throw new InjectionException( e );
        }
    }

    public void visitDependency( DependencyVisitor visitor )
    {
        visitor.visit( dependencyModel );
    }
}
