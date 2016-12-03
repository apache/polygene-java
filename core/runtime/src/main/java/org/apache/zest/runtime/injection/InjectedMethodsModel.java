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

package org.apache.zest.runtime.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.zest.api.injection.InjectionScope;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.util.Methods;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.functional.VisitableHierarchy;

import static org.apache.zest.api.util.Annotations.typeHasAnnotation;

/**
 * JAVADOC
 */
public final class InjectedMethodsModel
    implements Dependencies, VisitableHierarchy<Object, Object>
{
    // Model
    private final List<InjectedMethodModel> methodModels = new ArrayList<>();

    public InjectedMethodsModel( Class fragmentClass )
    {
        Methods.methodsOf( fragmentClass ).forEach( method -> {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            if( parameterAnnotations.length > 0 )
            {
                InjectedParametersModel parametersModel = new InjectedParametersModel();
                final Type[] genericParameterTypes = method.getGenericParameterTypes();
                boolean found = true;
                for( int i = 0; i < parameterAnnotations.length; i++ )
                {
                    Optional<Annotation> opt = Arrays.stream( parameterAnnotations[ i ] )
                        .filter( typeHasAnnotation( InjectionScope.class ) )
                        .findFirst();
                    if( opt.isPresent() )
                    {
                        Annotation injectionAnnotation = opt.get();
                        Type genericType = genericParameterTypes[ i ];
                        if( genericType instanceof ParameterizedType )
                        {
                            genericType = createParameterizedTypeInstance( (ParameterizedType) genericType );

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
                    else
                    {
                        found = false;
                        break;
                    }
                }
                if( found )
                {
                    methodModels.add( new InjectedMethodModel( method, parametersModel ) );
                }
            }
        } );
    }

    private Type createParameterizedTypeInstance( ParameterizedType genericType )
    {
        return new ParameterizedTypeInstance(
            genericType.getActualTypeArguments(), genericType.getRawType(), genericType.getOwnerType()
        );
    }

    @Override
    public Stream<DependencyModel> dependencies()
    {
        return methodModels.stream().flatMap( InjectedMethodModel::dependencies );
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