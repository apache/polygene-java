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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.DependencyDescriptor;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Visitable;
import org.qi4j.functional.Visitor;
import org.qi4j.runtime.injection.provider.InjectionProviderException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.functional.Iterables.iterable;

/**
 * JAVADOC
 * move all the extraction code to a TypeUtils class
 */
public final class DependencyModel
    implements Binder, DependencyDescriptor, Visitable<DependencyModel>
{
    public static boolean isOptional( Annotation injectionAnnotation, Annotation[] annotations )
    {
        if( Iterables.matchesAny( isType( Optional.class ), iterable( annotations ) ) )
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
    private final boolean optional;
    private final Annotation[] annotations;

    // Binding
    private InjectionProvider injectionProvider;

    public DependencyModel( Annotation injectionAnnotation,
                            Type genericType,
                            Class<?> injectedClass,
                            boolean optional,
                            Annotation[] annotations
    )
    {
        this.injectionAnnotation = injectionAnnotation;
        this.injectedClass = injectedClass;

        this.injectionType = genericType;
        this.optional = optional;
        this.annotations = annotations;
        this.rawInjectionClass = mapPrimitiveTypes( extractRawInjectionClass( injectedClass, injectionType ) );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super DependencyModel, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( this );
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
        throw new IllegalArgumentException(
            "Could not extract the rawInjectionClass of " + injectedClass + " and " + injectionType );
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
        throw new IllegalArgumentException(
            "Could not extract the rawInjectionClass of " + injectedClass + " and " + injectionTypeVariable );
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

    // FIXME This method is unused, remove it.
    private Type extractDependencyType( Type injectionType )
    {
        if( injectionType instanceof ParameterizedType )
        {
            return ( (ParameterizedType) injectionType ).getActualTypeArguments()[ 0 ];
        }
        else if( injectionType instanceof TypeVariable )
        {
            return ( (TypeVariable) injectionType ).getBounds()[ 0 ];
        }
        return injectionType;
    }

    // Model
    @Override
    public Annotation injectionAnnotation()
    {
        return injectionAnnotation;
    }

    @Override
    public Type injectionType()
    {
        return injectionType;
    }

    @Override
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
    @Override
    public Class<?> rawInjectionType()
    {
        return rawInjectionClass;
    }

    @Override
    public boolean optional()
    {
        return optional;
    }

    @Override
    public Annotation[] annotations()
    {
        return annotations;
    }

    @Override
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
                    "[Module " + resolution.module()
                        .name() + "] Non-optional @" + rawInjectionClass.getName() + " was not bound in " + injectedClass
                        .getName();
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
            Throwable ex = e;
            if( ex.getCause() != null )
            {
                ex = ex.getCause();
            }

            String message = "[Module " + context.module()
                .name() + "] InjectionProvider unable to resolve @" + injectionAnnotation.annotationType()
                .getSimpleName() + " " + injectionType.toString();
            throw new ConstructionException( message, ex );
        }
        if( injectedValue == null && !optional )
        {
            String message = "[Module " + context.module()
                .name() + "] Non-optional @" + injectionAnnotation.annotationType()
                .getSimpleName() + " " + injectionType.toString() + " was null in " + injectedClass.getName();
            throw new ConstructionException( message );
        }
        return getInjectedValue( injectedValue );
    }

    @SuppressWarnings( "unchecked" )
    private Object getInjectedValue( Object injectionResult )
    {
        if( injectionResult == null )
        {
            return null;
        }

        if( injectionResult instanceof Iterable )
        {
            if( Iterable.class.isAssignableFrom( rawInjectionClass ) || rawInjectionClass.isInstance(
                injectionResult ) )
            {
                return injectionResult;
            }
            else
            {
                return Iterables.first( (Iterable) injectionResult );
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

        @Override
        public boolean satisfiedBy( DependencyModel model )
        {
            return model.hasScope( scope );
        }
    }

    public static class InjectionTypeFunction
        implements Function<DependencyModel, Class<?>>
    {
        @Override
        public Class<?> map( DependencyModel dependencyModel )
        {
            return dependencyModel.rawInjectionType();
        }
    }
}
