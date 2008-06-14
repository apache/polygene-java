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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.provider.InvalidInjectionException;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.Specification;
import static org.qi4j.runtime.util.CollectionUtils.*;

/**
 * TODO
 * move all the extraction code to a TypeUtils class
 */
public final class DependencyModel
    implements Binder
{
    // Model
    private Annotation injectionAnnotation;
    private Type injectionType;
    private Class<?> injectedClass;
    private Class<?> rawInjectionClass;
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
     * and otherwise it is the type of the field. Examples:
     *
     * @return raw injection type
     * @Service MyService service -> MyService
     * @Entity Iterable<Foo> fooList -> Iterable
     * @Entity Query<Foo> fooQuery -> Query
     */
    public Class<?> rawInjectionType()
    {
        return rawInjectionClass;
    }

    /**
     * Get the injection class. If the injection uses generics this is the parameter type,
     * and otherwise it is the raw type. Examples:
     *
     * @return injection class
     * @Service MyService service -> MyService
     * @Entity Iterable<Foo> fooList -> Foo
     * @Entity Query<Foo> fooQuery -> Foo
     */
    public Class<?> injectionClass()
    {
        return injectionClass;
    }

    public boolean optional()
    {
        return optional;
    }

    // Binding
    public void bind( Resolution resolution )
        throws BindingException
    {
        InjectionProviderFactory providerFactory = resolution.application().injectionProviderFactory();

        try
        {
            injectionProvider = providerFactory.newInjectionProvider( resolution, this );
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
            throw new org.qi4j.composite.InstantiationException( "Non-optional @" + rawInjectionClass.getName() + " was null" );
        }

        return getInjectedValue( injectedValue );
    }

    private <K> Object getInjectedValue( Object injectionResult )
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

    private final static Class<?>[] primitiveTypeMapping = { boolean.class, Boolean.class,
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

    public static class ScopeSpecification
        implements Specification<DependencyModel>
    {
        private Class<? extends Annotation> scope;

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
