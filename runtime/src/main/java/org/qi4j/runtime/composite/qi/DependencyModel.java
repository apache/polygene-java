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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.Iterator;
import org.qi4j.composite.scope.This;
import org.qi4j.runtime.injection.ThisInjectionProviderFactory;
import org.qi4j.spi.composite.BindingException;
import org.qi4j.spi.injection.InvalidInjectionException;

/**
 * TODO
 */
public final class DependencyModel
{
    // Model
    private Annotation injectionAnnotation;
    private Type injectionType;
    private Class<?> injectedClass;
    private Class<?> rawInjectionType;
    private Class<?> injectionClass;
    private boolean optional;

    // Binding
    private InjectionProvider injectionProvider;

    public DependencyModel( Annotation injectionAnnotation, Type genericType, Class<?> injectedClass, boolean optional )
    {
        this.injectionAnnotation = injectionAnnotation;
        this.injectedClass = injectedClass;
        this.injectionType = genericType;
        this.optional = optional;

        // Calculate raw injection type
        if( injectionType instanceof Class )
        {
            rawInjectionType = (Class<?>) injectionType;
        }
        else if( injectionType instanceof ParameterizedType )
        {
            rawInjectionType = (Class<?>) ( (ParameterizedType) injectionType ).getRawType();
        }
        else if( injectionType instanceof TypeVariable )
        {
            TypeVariable<?> injectionTypeVariable = (TypeVariable<?>) injectionType;

            int index = 0;
            TypeVariable<?>[] typeVariables = ( (TypeVariable<?>) injectionType ).getGenericDeclaration().getTypeParameters();
            for( TypeVariable<?> typeVariable : typeVariables )
            {
                if( injectionTypeVariable.getName().equals( typeVariable.getName() ) )
                {
                    // Type index found - map it to actual type
                    Type genericClass = injectedClass;
                    Type type = null;

                    while( !Object.class.equals( genericClass ) && type == null )
                    {
                        genericClass = ( (Class<?>) genericClass ).getGenericSuperclass();
                        if( genericClass instanceof ParameterizedType )
                        {
                            type = ( (ParameterizedType) genericClass ).getActualTypeArguments()[ index ];
                        }
                        else
                        {
                            Type[] genericInterfaces = ( (Class<?>) genericClass ).getGenericInterfaces();
                            if( genericInterfaces.length > 0 )
                            {
                                type = genericInterfaces[ index ];
                                if( type instanceof ParameterizedType )
                                {
                                    type = ( (ParameterizedType) type ).getActualTypeArguments()[ index ];
                                }
                            }
                        }
                    }
                    rawInjectionType = (Class<?>) type;
                }
                index++;
            }
        }

        if( rawInjectionType.isPrimitive() )
        {
            rawInjectionType = getNonPrimitiveType( rawInjectionType );
        }

        // Calculate injection class
        if( injectionType instanceof ParameterizedType )
        {
            Type type = ( (ParameterizedType) injectionType ).getActualTypeArguments()[ 0 ];
            if( type instanceof Class )
            {
                injectionClass = (Class<?>) type;
            }
            else if( type instanceof ParameterizedType )
            {
                injectionClass = (Class<?>) ( (ParameterizedType) type ).getRawType();
            }
        }
        else if( injectionType instanceof TypeVariable )
        {
            injectionClass = (Class<?>) ( (TypeVariable<?>) injectionType ).getBounds()[ 0 ];
        }
        else
        {
            injectionClass = (Class<?>) injectionType;
        }
    }

    // Model
    public Annotation injectionAnnotation()
    {
        return injectionAnnotation;
    }

    public Type injectionType()
    {
        return injectionType;
    }

    public Class<?> injectedClass()
    {
        return injectedClass;
    }

    public Class<?> rawInjectionType()
    {
        return rawInjectionType;
    }

    public Class<?> injectionClass()
    {
        return injectionClass;
    }

    public boolean optional()
    {
        return optional;
    }

    // Binding
    public void bind( BindingContext context )
        throws BindingException
    {
        InjectionProviderFactory providerFactory;
        if( injectionAnnotation.annotationType().equals( This.class ) )
        {
            providerFactory = new ThisInjectionProviderFactory();
        }
        else
        {
            throw new BindingException( "Could not bind unknown injection annotation:" + injectionAnnotation.annotationType().getName() );
        }

        try
        {
            injectionProvider = providerFactory.newInjectionProvider( context, this );
        }
        catch( InvalidInjectionException e )
        {
            throw new BindingException( "Could not bind", e );
        }
    }

    // Context
    public Object inject( InjectionContext context )
    {
        Object injectedValue = injectionProvider.provideInjection( context );

        if( injectedValue == null && !optional )
        {
            throw new org.qi4j.composite.InstantiationException( "Non-optional @" + rawInjectionType.getName() + " was null" );
        }

        return getInjectedValue( injectedValue );
    }

    private <K> Object getInjectedValue( Object injectionResult )
    {
        if( injectionResult == null )
        {
            return null;
        }

        if( Iterable.class.equals( injectionType ) && !Iterable.class.isAssignableFrom( injectionResult.getClass() ) )
        {
            return Collections.singleton( injectionResult );
        }

        if( injectionResult instanceof Iterable && !Iterable.class.isAssignableFrom( rawInjectionType ) && !rawInjectionType.isInstance( injectionResult ) )
        {
            Iterator iterator = ( (Iterable) injectionResult ).iterator();
            if( iterator.hasNext() )
            {
                return iterator.next();
            }
            else
            {
                return null;
            }
        }

        return injectionResult;
    }

    private Class<?> getNonPrimitiveType( Class<?> rawInjectionType )
    {
        if( rawInjectionType.getSimpleName().equals( "boolean" ) )
        {
            return Boolean.class;
        }
        else
        {
            return rawInjectionType;
        }
    }
}
