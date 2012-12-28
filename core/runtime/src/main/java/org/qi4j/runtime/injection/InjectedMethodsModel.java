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
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.injection.InjectionScope;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Methods;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Specifications;
import org.qi4j.functional.VisitableHierarchy;

import static org.qi4j.api.util.Annotations.hasAnnotation;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.flattenIterables;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Iterables.map;

/**
 * JAVADOC
 */
public final class InjectedMethodsModel
    implements Dependencies, VisitableHierarchy<Object, Object>
{
    // Model
    private final List<InjectedMethodModel> methodModels = new ArrayList<InjectedMethodModel>();

    public InjectedMethodsModel( Class fragmentClass )
    {
        nextMethod:
        for( Method method : Methods.METHODS_OF.map( fragmentClass ) )
        {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            if( parameterAnnotations.length > 0 )
            {
                InjectedParametersModel parametersModel = new InjectedParametersModel();
                final Type[] genericParameterTypes = method.getGenericParameterTypes();
                for( int i = 0; i < parameterAnnotations.length; i++ )
                {
                    Annotation injectionAnnotation = first( filter( Specifications.translate( Annotations.type(), hasAnnotation( InjectionScope.class ) ), iterable( parameterAnnotations[ i ] ) ) );
                    if( injectionAnnotation == null )
                    {
                        continue nextMethod;
                    }

                    Type genericType = genericParameterTypes[ i ];
                    if( genericType instanceof ParameterizedType )
                    {
                        genericType = new ParameterizedTypeInstance( ( (ParameterizedType) genericType ).getActualTypeArguments(), ( (ParameterizedType) genericType )
                            .getRawType(), ( (ParameterizedType) genericType ).getOwnerType() );

                        for( int j = 0; j < ( (ParameterizedType) genericType ).getActualTypeArguments().length; j++ )
                        {
                            Type type = ( (ParameterizedType) genericType ).getActualTypeArguments()[ j ];
                            if( type instanceof TypeVariable )
                            {
                                type = Classes.resolveTypeVariable( (TypeVariable) type, method.getDeclaringClass(), fragmentClass );
                                ( (ParameterizedType) genericType ).getActualTypeArguments()[ j ] = type;
                            }
                        }
                    }

                    boolean optional = DependencyModel.isOptional( injectionAnnotation, parameterAnnotations[ i ] );
                    DependencyModel dependencyModel = new DependencyModel( injectionAnnotation, genericType, fragmentClass, optional, parameterAnnotations[ i ] );
                    parametersModel.addDependency( dependencyModel );
                }
                InjectedMethodModel methodModel = new InjectedMethodModel( method, parametersModel );
                methodModels.add( methodModel );
            }
        }
    }

    @Override
    public Iterable<DependencyModel> dependencies()
    {
        return flattenIterables( map( Dependencies.DEPENDENCIES_FUNCTION, methodModels ) );
    }

    // Context
    public void inject( InjectionContext context, Object instance )
    {
        for( InjectedMethodModel methodModel : methodModels )
        {
            methodModel.inject( context, instance );
        }
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( InjectedMethodModel methodModel : methodModels )
            {
                if( !methodModel.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }
}