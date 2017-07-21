/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.composite;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.composite.ConstructorDescriptor;
import org.apache.polygene.api.composite.InvalidCompositeException;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.injection.DependencyModel;
import org.apache.polygene.runtime.injection.InjectedParametersModel;
import org.apache.polygene.runtime.injection.InjectionContext;

import static org.apache.polygene.api.util.AccessibleObjects.accessible;

/**
 * JAVADOC
 */
public final class ConstructorModel
    implements ConstructorDescriptor, VisitableHierarchy<Object, Object>
{
    private static final String NL = System.getProperty( "line.separator" );

    private Constructor<?> constructor;

    private InjectedParametersModel parameters;

    public ConstructorModel( Constructor<?> constructor, InjectedParametersModel parameters )
    {
        this.constructor = accessible( constructor );
        this.parameters = parameters;
    }

    @Override
    public Constructor<?> constructor()
    {
        return constructor;
    }

    public Stream<DependencyModel> dependencies()
    {
        return parameters.dependencies();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            parameters.accept( modelVisitor );
        }

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
            Throwable targetException = e.getTargetException();
            if( targetException instanceof InvalidCompositeException )
            {
                throw (InvalidCompositeException) targetException;
            }
            throw new ConstructionException( createExceptionMessage( parametersInstance ), targetException );
        }
        catch( Throwable e )
        {
            throw new ConstructionException( createExceptionMessage( parametersInstance ), e );
        }
    }

    private String createExceptionMessage( Object[] parametersInstance )
    {
        return "Could not instantiate " + NL + "    " + constructor.getDeclaringClass()
               + NL + "using constructor:" + NL + "    " + constructor.toGenericString()
               + NL + "parameter types:" + NL + "    " + Arrays.toString( parametersInstance );
    }

    @Override
    public String toString()
    {
        return constructor.toGenericString();
    }
}
