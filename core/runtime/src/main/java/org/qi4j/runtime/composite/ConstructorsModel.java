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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.InvalidCompositeException;
import org.qi4j.api.injection.InjectionScope;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.functional.Function;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specifications;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedParametersModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.ParameterizedTypeInstance;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;

import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.iterable;

/**
 * JAVADOC
 */
public final class ConstructorsModel
    implements Binder, Dependencies, VisitableHierarchy<Object, Object>
{
    private final Class fragmentClass;
    private final List<ConstructorModel> constructorModels;
    private List<ConstructorModel> boundConstructors;

    public ConstructorsModel( Class fragmentClass )
    {
        this.fragmentClass = fragmentClass;
        validate( fragmentClass );
        constructorModels = new ArrayList<ConstructorModel>();
        Constructor[] realConstructors = this.fragmentClass.getDeclaredConstructors();
        Class injectionClass = FragmentClassLoader.getSourceClass( fragmentClass );
        for( Constructor constructor : realConstructors )
        {
            constructor.setAccessible( true );
            try
            {
                Constructor injectionConstructor = injectionClass.getDeclaredConstructor( constructor.getParameterTypes() );
                injectionConstructor.setAccessible( true );
                ConstructorModel constructorModel = newConstructorModel( this.fragmentClass, constructor,
                                                                         injectionConstructor );
                if( constructorModel != null )
                {
                    constructorModels.add( constructorModel );
                }
            }
            catch( NoSuchMethodException e )
            {
                // Ignore and continue
                e.printStackTrace();
            }
        }
    }

    private void validate( Class fragmentClass )
    {
        // Ensure that the fragment class is not an inner class, in which case we should give a reasonable exception
        if( fragmentClass.getDeclaringClass() == null )
        {
            return;
        }
        if( Modifier.isStatic( fragmentClass.getModifiers() ) )
        {
            return;
        }
        throw new InvalidCompositeException( "Inner classes can not be used. Use static nested classes instead: " + fragmentClass );
    }

    @Override
    public Iterable<DependencyModel> dependencies()
    {
        Function<ConstructorModel, Iterable<DependencyModel>> constructorDependencies = new Function<ConstructorModel, Iterable<DependencyModel>>()
        {
            @Override
            public Iterable<DependencyModel> map( ConstructorModel constructorModel )
            {
                return constructorModel.dependencies();
            }
        };

        return Iterables.flattenIterables( Iterables.map( constructorDependencies, boundConstructors == null ? constructorModels : boundConstructors ) );
    }

    private ConstructorModel newConstructorModel( Class fragmentClass,
                                                  Constructor realConstructor,
                                                  Constructor injectedConstructor
    )
    {
        int idx = 0;
        InjectedParametersModel parameters = new InjectedParametersModel();
        Annotation[][] parameterAnnotations = injectedConstructor.getParameterAnnotations();
        for( Type type : injectedConstructor.getGenericParameterTypes() )
        {
            Annotation injectionAnnotation = first(
                filter( Specifications.translate( Annotations.type(), Annotations.hasAnnotation( InjectionScope.class ) ), iterable( parameterAnnotations[ idx ] ) ) );

            if( injectionAnnotation == null )
            {
                if( fragmentClass.getSuperclass().isMemberClass() )
                {
                    injectionAnnotation = new Uses()
                    {
                        @Override
                        public Class<? extends Annotation> annotationType()
                        {
                            return Uses.class;
                        }
                    };
                }
                else
                {
                    return null; // invalid constructor parameter
                }
            }

            boolean optional = DependencyModel.isOptional( injectionAnnotation, parameterAnnotations[ idx ] );

            Type genericType = type;
            if( genericType instanceof ParameterizedType )
            {
                genericType = new ParameterizedTypeInstance( ( (ParameterizedType) genericType ).getActualTypeArguments(), ( (ParameterizedType) genericType )
                    .getRawType(), ( (ParameterizedType) genericType ).getOwnerType() );

                for( int i = 0; i < ( (ParameterizedType) genericType ).getActualTypeArguments().length; i++ )
                {
                    Type typeArg = ( (ParameterizedType) genericType ).getActualTypeArguments()[ i ];
                    if( typeArg instanceof TypeVariable )
                    {
                        typeArg = Classes.resolveTypeVariable( (TypeVariable) typeArg, realConstructor.getDeclaringClass(), fragmentClass );
                        ( (ParameterizedType) genericType ).getActualTypeArguments()[ i ] = typeArg;
                    }
                }
            }

            DependencyModel dependencyModel = new DependencyModel( injectionAnnotation, genericType, fragmentClass, optional,
                                                                   parameterAnnotations[ idx ] );
            parameters.addDependency( dependencyModel );
            idx++;
        }
        return new ConstructorModel( realConstructor, parameters );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( boundConstructors != null )
            {
                for( ConstructorModel constructorModel : boundConstructors )
                {
                    if( !constructorModel.accept( visitor ) )
                    {
                        break;
                    }
                }
            }
            else
            {
                for( ConstructorModel constructorModel : constructorModels )
                {
                    if( !constructorModel.accept( visitor ) )
                    {
                        break;
                    }
                }
            }
        }
        return visitor.visitLeave( this );
    }

    // Binding
    @Override
    public void bind( final Resolution resolution )
        throws BindingException
    {
        boundConstructors = new ArrayList<ConstructorModel>();
        for( ConstructorModel constructorModel : constructorModels )
        {
            try
            {
                constructorModel.accept( new HierarchicalVisitorAdapter<Object, Object, BindingException>()
                {
                    @Override
                    public boolean visit( Object visitor )
                        throws BindingException
                    {
                        if( visitor instanceof Binder )
                        {
                            ( (Binder) visitor ).bind( resolution );
                        }
                        return true;
                    }
                } );
                boundConstructors.add( constructorModel );
            }
            catch( Exception e )
            {
                // Ignore
                e.printStackTrace();
            }
        }

        if( boundConstructors.isEmpty() )
        {
            StringBuilder messageBuilder = new StringBuilder( "Found no constructor that could be bound: " );
            if( resolution.model() instanceof CompositeDescriptor )
            {
                messageBuilder.append( fragmentClass.getName() )
                    .append( " in " )
                    .append( resolution.model().toString() );
            }
            else
            {
                messageBuilder.append( resolution.model().toString() );
            }

            if( messageBuilder.indexOf( "$" ) >= 0 )
            {
                // This could be ok if instance is created manually
                return;
//                messageBuilder.append( "\nInner classes can not be used." );
            }
            String message = messageBuilder.toString();
            throw new BindingException( message );
        }

        // Sort based on parameter count
        Collections.sort( boundConstructors, new Comparator<ConstructorModel>()
        {
            @Override
            public int compare( ConstructorModel o1, ConstructorModel o2 )
            {
                Integer model2ParametersCount = o2.constructor().getParameterTypes().length;
                int model1ParametersCount = o1.constructor().getParameterTypes().length;
                return model2ParametersCount.compareTo( model1ParametersCount );
            }
        } );
    }

    public Object newInstance( InjectionContext injectionContext )
    {
        // Try all bound constructors, in order
        ConstructionException exception = null;
        for( ConstructorModel constructorModel : boundConstructors )
        {
            try
            {
                return constructorModel.newInstance( injectionContext );
            }
            catch( ConstructionException e )
            {
                exception = e;
            }
        }

        throw exception;
    }
}
