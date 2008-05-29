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
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.spi.composite.BindingException;
import static org.qi4j.util.ClassUtil.constructorsOf;

/**
 * TODO
 */
public class ConstructorsModel
{
    List<ConstructorModel> constructorModels;

    ConstructorModel boundConstructor;

    public ConstructorsModel( Class fragmentClass )
    {
        constructorModels = new ArrayList<ConstructorModel>();
        List<Constructor> constructors = constructorsOf( fragmentClass );
        for( Constructor constructor : constructors )
        {
            int idx = 0;
            List<InjectedParameterModel> parameterModels = new ArrayList<InjectedParameterModel>();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            for( Type type : constructor.getGenericParameterTypes() )
            {
                DependencyModel dependency = null; // TODO
                ParameterModel parameterModel = new ParameterModel( parameterAnnotations[ idx ], type );
                InjectedParameterModel injectedParameterModel = new InjectedParameterModel( parameterModel, dependency );
                parameterModels.add( injectedParameterModel );
                idx++;
            }
            InjectedParametersModel parameters = new InjectedParametersModel( parameterModels );
            ConstructorModel constructorModel = new ConstructorModel( constructor, parameters );
            constructorModels.add( constructorModel );
        }
    }

    public void visitDependencies( DependencyVisitor dependencyVisitor )
    {
        for( ConstructorModel constructorModel : constructorModels )
        {
            constructorModel.visitDependencies( dependencyVisitor );
        }
    }

    // Binding
    public void bind( BindingContext context )
    {
        for( ConstructorModel constructorModel : constructorModels )
        {
            try
            {
                constructorModel.bind( context );
                boundConstructor = constructorModel;
            }
            catch( Exception e )
            {
                // Ignore
            }
        }

        if( boundConstructor == null )
        {
            throw new BindingException( "Found no constructor that could be bound" );
        }
    }

    public Object newInstance( InjectionContext injectionContext )
    {
        return boundConstructor.newInstance( injectionContext );
    }
}
