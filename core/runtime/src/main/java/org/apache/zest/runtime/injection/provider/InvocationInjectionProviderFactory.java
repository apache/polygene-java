/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.runtime.injection.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.apache.zest.api.util.Classes;
import org.apache.zest.bootstrap.InvalidInjectionException;
import org.apache.zest.runtime.composite.CompositeMethodModel;
import org.apache.zest.runtime.injection.DependencyModel;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.runtime.injection.InjectionProvider;
import org.apache.zest.runtime.injection.InjectionProviderFactory;
import org.apache.zest.runtime.model.Resolution;

/**
 * JAVADOC
 */
public final class InvocationInjectionProviderFactory
    implements InjectionProviderFactory
{
    @Override
    @SuppressWarnings( "raw" )
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        Class injectionClass = Classes.RAW_CLASS.apply( dependencyModel.injectionType() );
        if( injectionClass.equals( Method.class ) ||
            injectionClass.equals( AnnotatedElement.class ) ||
            injectionClass.equals( Iterable.class ) ||
            Annotation.class.isAssignableFrom( injectionClass ) )
        {
            return new InvocationDependencyResolution( resolution, dependencyModel );
        }
        else
        {
            String injectedTo = dependencyModel.injectedClass().getName();
            throw new InvalidInjectionException( "Invalid injection type " + injectionClass + " in " + injectedTo );
        }
    }

    private static class InvocationDependencyResolution
        implements InjectionProvider
    {
        private final Resolution resolution;
        private final DependencyModel dependencyModel;

        private InvocationDependencyResolution( Resolution resolution, DependencyModel dependencyModel )
        {
            this.resolution = resolution;
            this.dependencyModel = dependencyModel;
        }

        @Override
        @SuppressWarnings( {"raw", "unchecked"} )
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            Class injectionClass = Classes.RAW_CLASS.apply( dependencyModel.injectionType() );
            final CompositeMethodModel methodModel = resolution.method();
            if( injectionClass.equals( Method.class ) )
            {
                return methodModel.method();
            }

            final AnnotatedElement annotatedElement = methodModel.annotatedElement();
            if( injectionClass.equals( AnnotatedElement.class ) )
            {
                return annotatedElement;
            }
            final Annotation annotation = annotatedElement.getAnnotation( injectionClass );
            if( annotation != null )
            {
                return annotation;
            }
            if( dependencyModel.injectionType() instanceof Class<?> )
            {
                return annotatedElement.getAnnotation( (Class<Annotation>) dependencyModel.injectionType() );
            }
            if( dependencyModel.injectionType() instanceof ParameterizedType )
            {
                ParameterizedType injectionType = (ParameterizedType) dependencyModel.injectionType();
                Type rawType = injectionType.getRawType();
                Type[] actualTypeArguments = injectionType.getActualTypeArguments();
                boolean isAnIterable = rawType.equals( Iterable.class );
                boolean haveOneGenericType = actualTypeArguments.length == 1;
                boolean thatIsOfTypeMethod = actualTypeArguments[ 0 ].equals( Method.class );
                if( isAnIterable && haveOneGenericType && thatIsOfTypeMethod )
                {
                    Class<?> injectedClass = dependencyModel.injectedClass();
                    Iterable<Method> result = methodModel.invocationsFor( injectedClass );
                    return result;
                }
            }
            return null;
        }
    }
}