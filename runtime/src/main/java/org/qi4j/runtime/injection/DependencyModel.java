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
package org.qi4j.runtime.injection;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Optional;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.injection.provider.CachingInjectionProviderDecorator;
import org.qi4j.runtime.injection.provider.InjectionProviderException;
import org.qi4j.runtime.injection.provider.ServiceInjectionProviderFactory;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.Specification;
import org.qi4j.spi.composite.DependencyDescriptor;
import org.qi4j.spi.util.Annotations;

import static org.qi4j.spi.util.CollectionUtils.*;

/**
 * JAVADOC
 * move all the extraction code to a TypeUtils class
 */
public final class DependencyModel
    implements Binder, DependencyDescriptor, Serializable
{
    public static boolean isOptional( Annotation injectionAnnotation, Annotation[] annotations )
    {
        if( Annotations.getAnnotationOfType( annotations, Optional.class ) != null )
        {
            return true;
        }

        Method[] methods = injectionAnnotation.annotationType().getMethods();
        for( Method method : methods )
        {
            if( method.getName().equals( "optional" ) )
            {
                try
                {
                    return (Boolean) method.invoke( injectionAnnotation );
                }
                catch( Throwable e )
                {
                    return false;
                }
            }
        }

        return false;
    }

    // Model
    private final Annotation injectionAnnotation;
    private final Type injectionType;
    private final Class<?> injectedClass;
    private final Class<?> rawInjectionClass;
    private final Class<?> injectionClass;
    private final boolean optional;

    // Binding
    private InjectionProvider injectionProvider;

    public DependencyModel( Annotation injectionAnnotation, Type genericType, Class<?> injectedClass, boolean optional )
    {
        this.injectionAnnotation = injectionAnnotation;
        this.injectedClass = injectedClass;
        this.injectionType = genericType;
        this.optional = optional;
        this.rawInjectionClass = mapPrimitiveTypes( extractRawInjectionClass( injectedClass, injectionType ) );
        this.injectionClass = extractInjectionClass( injectionType );
    }

    private Class<?> extractRawInjectionClass( Class<?> injectedClass, final Type injectionType )
    {
        // Calculate raw injection type
        if( injectionType instanceof Class )
        {
            return (Class<?>) injectionType;
        }
        else if( injectionType instanceof ParameterizedType )
        {
            return (Class<?>) ( (ParameterizedType) injectionType ).getRawType();
        }
        else if( injectionType instanceof TypeVariable )
        {
            return extractRawInjectionClass( injectedClass, (TypeVariable<?>) injectionType );
        }
        throw new IllegalArgumentException( "Could not extract the rawInjectionClass of " + injectedClass + " and " + injectionType );
    }

    private Class<?> extractRawInjectionClass( Class<?> injectedClass, TypeVariable<?> injectionTypeVariable )
    {
        int index = 0;
        for( TypeVariable<?> typeVariable : injectionTypeVariable.getGenericDeclaration().getTypeParameters() )
        {
            if( injectionTypeVariable.getName().equals( typeVariable.getName() ) )
            {
                return (Class<?>) getActualType( injectedClass, index );
            }
            index++;
        }
        throw new IllegalArgumentException( "Could not extract the rawInjectionClass of " + injectedClass + " and " + injectionTypeVariable );
    }

    // todo continue refactoring

    private Type getActualType( Class<?> injectedClass, int index )
    {
        // Type index found - map it to actual type
        Type genericType = injectedClass;
        Type type = null;

        while( !Object.class.equals( genericType ) && type == null )
        {
            genericType = ( (Class<?>) genericType ).getGenericSuperclass();
            if( genericType instanceof ParameterizedType )
            {
                type = ( (ParameterizedType) genericType ).getActualTypeArguments()[ index ];
            }
            else
            {
                Type[] genericInterfaces = ( (Class<?>) genericType ).getGenericInterfaces();
                if( genericInterfaces.length > index )
                {
                    type = genericInterfaces[ index ];
                    if( type instanceof ParameterizedType )
                    {
                        type = ( (ParameterizedType) type ).getActualTypeArguments()[ index ];
                    }
                    // TODO type may still be one of the generic interfaces???
                }
            }
        }

        if( type == null )
        {
            type = Object.class; // Generic type with no constraints so Object is fine
        }

        return type;
    }

    private Class<?> extractInjectionClass( Type injectionType )
    {
        if( injectionType instanceof ParameterizedType )
        {
            return extractInjectionClass( (ParameterizedType) injectionType );
        }
        else if( injectionType instanceof TypeVariable )
        {
            return extractInjectionClass( (TypeVariable<?>) injectionType );
        }
        return (Class<?>) injectionType;
    }

    private Class<?> extractInjectionClass( TypeVariable<?> typeVariable )
    {
        return (Class<?>) typeVariable.getBounds()[ 0 ];
    }

    private Class<?> extractInjectionClass( ParameterizedType parameterizedType )
    {
        Type type = parameterizedType.getActualTypeArguments()[ 0 ];
        if( type instanceof Class )
        {
            return (Class<?>) type;
        }
        else if( type instanceof ParameterizedType )
        {
            return (Class<?>) ( (ParameterizedType) type ).getRawType();
        }
        else if( type instanceof WildcardType )
        {
            // To handle for instance Class<? extends Habba>, which will then return habba
            WildcardType wcType = (WildcardType) type;
            return (Class) wcType.getUpperBounds()[ 0 ];
        }
        else if( type instanceof TypeVariable )
        {
            TypeVariable tv = (TypeVariable) type;
            return (Class) tv.getBounds()[ 0 ];
        }
        throw new IllegalArgumentException( "Could not extract injectionClass of Type " + parameterizedType );
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

    /**
     * Get the raw dependency type. If the dependency uses generics this is the raw type,
     * and otherwise it is the type of the field. Examples:<br/>
     * {@code @Service MyService service} -> MyService<br/>
     * {@code @Entity Iterable<Foo> fooList} -> Iterable<br/>
     * {@code @Entity Query<Foo> fooQuery} -> Query<br/>
     *
     * @return raw injection type.
     */
    public Class<?> rawInjectionType()
    {
        return rawInjectionClass;
    }

    /**
     * Get the injection class. If the injection uses generics this is the parameter type,
     * and otherwise it is the raw type. Examples:<br/>
     * {@code @Service MyService service} -> MyService<br/>
     * {@code @Entity Iterable<Foo> fooList} -> Foo<br/>
     * {@code @Entity Query<Foo> fooQuery} -> Foo<br/>
     *
     * @return injection class
     */
    public Class<?> injectionClass()
    {
        return injectionClass;
    }

    public boolean optional()
    {
        return optional;
    }

    public Iterable<String> injectedServices()
    {
        Iterable<String> services = Collections.emptyList();

        if( injectionProvider instanceof CachingInjectionProviderDecorator )
        {
            InjectionProvider decoratedProvider = ( (CachingInjectionProviderDecorator) injectionProvider ).decoratedProvider();
            if( decoratedProvider instanceof ServiceInjectionProviderFactory.ServiceInjector )
            {
                services = ( (ServiceInjectionProviderFactory.ServiceInjector) decoratedProvider ).injectedServices();
            }
        }

        return services;
    }

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        InjectionProviderFactory providerFactory = resolution.application().injectionProviderFactory();

        try
        {
            injectionProvider = providerFactory.newInjectionProvider( resolution, this );

            if( injectionProvider == null && !optional )
            {
                String message =
                    "Non-optional @" + rawInjectionClass.getName() + " was not bound in " + injectedClass.getName();
                throw new ConstructionException( message );
            }
        }
        catch( InvalidInjectionException e )
        {
            throw new BindingException( "Could not bind dependency injection", e );
        }
    }

    // Context

    public Object inject( InjectionContext context )
    {
        if( injectionProvider == null )
        {
            return null;
        }
        Object injectedValue;
        try
        {
            injectedValue = injectionProvider.provideInjection( context );
        }
        catch( InjectionProviderException e )
        {
            String cause = e.getMessage();
            String message = "InjectionProvider unable to resolve @" + rawInjectionClass.getName() + " : " + cause;
            throw new ConstructionException( message );
        }
        if( injectedValue == null && !optional )
        {
            String message = "Non-optional @" + rawInjectionClass.getName() + " was null in " + injectedClass.getName();
            throw new ConstructionException( message );
        }
        return getInjectedValue( injectedValue );
    }

    private Object getInjectedValue( Object injectionResult )
    {
        if( injectionResult == null )
        {
            return null;
        }

        if( injectionResult instanceof Iterable )
        {
            if( Iterable.class.isAssignableFrom( rawInjectionClass ) || rawInjectionClass.isInstance( injectionResult ) )
            {
                return injectionResult;
            }
            else
            {
                return firstElementOrNull( (Iterable) injectionResult );
            }
        }
        else
        {
            if( Iterable.class.equals( injectionType ) )
            {
                return Collections.singleton( injectionResult );
            }
        }
        return injectionResult;
    }

    private final static Class<?>[] primitiveTypeMapping = {
        boolean.class, Boolean.class,
        byte.class, Byte.class,
        short.class, Short.class,
        char.class, Character.class,
        long.class, Long.class,
        double.class, Double.class,
        float.class, Float.class,
        int.class, Integer.class,
    };

    private Class<?> mapPrimitiveTypes( Class<?> rawInjectionType )
    {
        if( rawInjectionType == null || !rawInjectionType.isPrimitive() )
        {
            return rawInjectionType;
        }
        for( int i = 0; i < primitiveTypeMapping.length; i += 2 )
        {
            if( primitiveTypeMapping[ i ].equals( rawInjectionType ) )
            {
                return primitiveTypeMapping[ i + 1 ];
            }
        }
        return rawInjectionType;
    }

    public boolean hasScope( final Class<? extends Annotation> scope )
    {
        return scope == null || scope.equals( injectionAnnotation().annotationType() );
    }

    public Class<? extends Annotation> injectionAnnotationType()
    {
        if( injectionAnnotation == null )
        {
            return null;
        }
        return injectionAnnotation.annotationType();
    }

    @Override
    public String toString()
    {
        return injectionAnnotation + " for " + injectionType + " in " + injectedClass.getName();
    }

    public static class ScopeSpecification
        implements Specification<DependencyModel>
    {
        private final Class<? extends Annotation> scope;

        public ScopeSpecification( Class<? extends Annotation> scope )
        {
            this.scope = scope;
        }

        public boolean matches( DependencyModel model )
        {
            return model.hasScope( scope );
        }
    }
}
