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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.util.AnnotationUtil;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public final class InjectedMethodsModel
{
    // Model
    private List<InjectedMethodModel> methodModels = new ArrayList<InjectedMethodModel>();

    public InjectedMethodsModel( Class fragmentClass )
    {
        List<Method> methods = ClassUtil.methodsOf( fragmentClass );
        nextMethod:
        for( Method method : methods )
        {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            if( parameterAnnotations.length > 0 )
            {
                List<InjectedParameterModel> parameterModels = new ArrayList<InjectedParameterModel>();
                for( int i = 0; i < method.getGenericParameterTypes().length; i++ )
                {
                    Annotation injectionAnnotation = AnnotationUtil.getInjectionAnnotation( parameterAnnotations[ i ] );
                    if( injectionAnnotation == null )
                    {
                        continue nextMethod;
                    }

                    Type type = method.getGenericParameterTypes()[ i ];

                    DependencyModel dependencyModel = new DependencyModel( injectionAnnotation, type, fragmentClass, false );
                    InjectedParameterModel parameterModel = new InjectedParameterModel( new ParameterModel( method.getAnnotations(), type ), dependencyModel );
                    parameterModels.add( parameterModel );
                }
                InjectedParametersModel parametersModel = new InjectedParametersModel( parameterModels );
                InjectedMethodModel methodModel = new InjectedMethodModel( method, parametersModel );
                methodModels.add( methodModel );
            }
        }
    }

    // Binding
    public void bind( BindingContext context )
    {
        for( InjectedMethodModel methodModel : methodModels )
        {
            methodModel.bind( context );
        }
    }

    // Context
    public void inject( InjectionContext context, Object instance )
    {
        for( InjectedMethodModel methodModel : methodModels )
        {
            methodModel.inject( context, instance );
        }
    }

    public void visitDependencies( DependencyVisitor visitor )
    {
        for( InjectedMethodModel methodModel : methodModels )
        {
            methodModel.visitDependencies( visitor );
        }
    }
}