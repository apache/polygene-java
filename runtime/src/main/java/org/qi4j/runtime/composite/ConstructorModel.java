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

package org.qi4j.runtime.composite;

import java.lang.reflect.Constructor;
import org.qi4j.runtime.injection.InjectedParametersModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;

/**
 * TODO
 */
public final class ConstructorModel
    implements Binder
{
    private Constructor constructor;

    private InjectedParametersModel parameters;

    public ConstructorModel( Constructor constructor, InjectedParametersModel parameters )
    {
        constructor.setAccessible( true );
        this.constructor = constructor;
        this.parameters = parameters;
    }


    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
        parameters.visitModel( modelVisitor );
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        parameters.bind( resolution );
    }

    // Context
    public Object newInstance( InjectionContext context )
        throws org.qi4j.composite.InstantiationException
    {
        // Create parameters
        Object[] parametersInstance = parameters.newParametersInstance( context );

        // Invoke constructor
        try
        {
            return constructor.newInstance( parametersInstance );
        }
        catch( Exception e )
        {
            throw new org.qi4j.composite.InstantiationException( "Could not instantiate " + constructor.getDeclaringClass(), e );
        }
    }
}
