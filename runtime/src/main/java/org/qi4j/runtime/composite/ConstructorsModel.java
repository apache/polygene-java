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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedParametersModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.util.AnnotationUtil;

/**
 * TODO
 */
public final class ConstructorsModel
    implements Binder
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
                DependencyModel dependencyModel = new DependencyModel( AnnotationUtil.getInjectionAnnotation( parameterAnnotations[ idx ] ), type, fragmentClass, false );
                parameters.addDependency( dependencyModel );
                idx++;
            }
            ConstructorModel constructorModel = new ConstructorModel( constructor, parameters );
            constructorModels.add( constructorModel );
        }
    }


    public void visitModel( ModelVisitor modelVisitor )
    {
        if( boundConstructor != null )
        {
            boundConstructor.visitModel( modelVisitor );
        }
        else
        {
            for( ConstructorModel constructorModel : constructorModels )
            {
                constructorModel.visitModel( modelVisitor );
            }
        }
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
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

}
