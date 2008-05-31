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

package org.qi4j.runtime.composite.qi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.InstantiationException;
import org.qi4j.spi.composite.BindingException;

/**
 * TODO
 */
public final class ConstructorsModel
{
    List<ConstructorModel> constructorModels;

    ConstructorModel boundConstructor;

    public ConstructorsModel( Class fragmentClass )
    {
        constructorModels = new ArrayList<ConstructorModel>();
        Constructor[] constructors = fragmentClass.getDeclaredConstructors();
        for( Constructor constructor : constructors )
        {
            int idx = 0;
            InjectedParametersModel parameters = new InjectedParametersModel();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            for( Type type : constructor.getGenericParameterTypes() )
            {
                DependencyModel dependency = null; // TODO
                parameters.addDependency( dependency );
                idx++;
            }
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
    public void bind( Resolution resolution )
    {
        for( ConstructorModel constructorModel : constructorModels )
        {
            try
            {
                constructorModel.bind( resolution );
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

    /**
     * TODO
     */
    private static final class ConstructorModel
    {
        private Constructor constructor;

        private InjectedParametersModel parameters;

        public ConstructorModel( Constructor constructor, InjectedParametersModel parameters )
        {
            constructor.setAccessible( true );
            this.constructor = constructor;
            this.parameters = parameters;
        }

        public void visitDependencies( DependencyVisitor dependencyVisitor )
        {
            parameters.visitDependencies( dependencyVisitor );
        }

        // Binding
        public void bind( Resolution resolution )
        {
            parameters.bind( resolution );
        }

        // Context
        public Object newInstance( InjectionContext context )
            throws org.qi4j.composite.InstantiationException
        {
            // Create parameters
            Object[] parametersInstance = parameters.newParametersInstance( context );

            // Invoke constructor
            try
            {
                return constructor.newInstance( parametersInstance );
            }
            catch( Exception e )
            {
                throw new InstantiationException( "Could not instantiate " + constructor.getDeclaringClass(), e );
            }
        }
    }
}
