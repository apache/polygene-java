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

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.ConstructorDescriptor;
import org.qi4j.api.composite.InvalidCompositeException;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedParametersModel;
import org.qi4j.runtime.injection.InjectionContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * JAVADOC
 */
public final class ConstructorModel
    implements ConstructorDescriptor, VisitableHierarchy<Object, Object>
{
    private Constructor constructor;

    private InjectedParametersModel parameters;

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

    public Iterable<DependencyModel> dependencies()
    {
        return parameters.dependencies();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor ) throws ThrowableType
    {
        if (modelVisitor.visitEnter( this ))
            parameters.accept( modelVisitor );

        return modelVisitor.visitLeave( this );
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
        catch( Throwable e )
        {
            System.err.println( constructor.toGenericString() );
            System.err.println( Arrays.asList( parametersInstance ) );
            throw new ConstructionException( "Could not instantiate " + constructor.getDeclaringClass(), e );
        }
    }

    @Override
    public String toString()
    {
        return constructor.toGenericString();
    }
}
