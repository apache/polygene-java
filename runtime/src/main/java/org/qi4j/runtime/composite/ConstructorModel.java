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
import java.lang.reflect.InvocationTargetException;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.runtime.injection.InjectedParametersModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.ConstructorDescriptor;
import org.qi4j.spi.composite.InvalidCompositeException;

/**
 * TODO
 */
public final class ConstructorModel
    implements Binder, ConstructorDescriptor
{
    private final Constructor constructor;

    private final InjectedParametersModel parameters;

    public ConstructorModel( Constructor constructor, InjectedParametersModel parameters )
    {
        constructor.setAccessible( true );
        this.constructor = constructor;
        constructor.setAccessible( true );
        this.parameters = parameters;
    }

    public Constructor constructor()
    {
        return constructor;
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
        throws ConstructionException
    {
        // Create parameters
        Object[] parametersInstance = parameters.newParametersInstance( context );

        // Invoke constructor
        try
        {
            return constructor.newInstance( parametersInstance );
        }
        catch( InvocationTargetException e )
        {
            if( e.getTargetException() instanceof InvalidCompositeException )
            {
                throw (InvalidCompositeException) e.getTargetException();
            }
            throw new ConstructionException( "Could not instantiate " + constructor.getDeclaringClass(), e.getTargetException() );
        }
        catch( Exception e )
        {
            throw new ConstructionException( "Could not instantiate " + constructor.getDeclaringClass(), e );
        }
    }

    @Override public String toString()
    {
        return constructor.toGenericString();
    }
}
