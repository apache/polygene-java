/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.composite;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.qi4j.spi.injection.InjectionModel;
import org.qi4j.spi.structure.Visibility;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see org.qi4j.spi.composite.MixinModel
 * @see org.qi4j.spi.composite.ConcernModel
 */
public abstract class AbstractModel
    implements Serializable
{
    private Class modelClass;

    private Iterable<ConstructorModel> constructorModels;
    private Iterable<FieldModel> fieldModels;
    private Iterable<MethodModel> methodModels;
    private Map<Class<? extends Annotation>, Set<InjectionModel>> dependenciesByScope;

    protected AbstractModel( Class modelClass, Iterable<ConstructorModel> constructorModels, Iterable<FieldModel> fieldModels, Iterable<MethodModel> methodModels )
    {
        this.modelClass = modelClass;
        this.constructorModels = constructorModels;
        this.fieldModels = fieldModels;
        this.methodModels = methodModels;
        this.dependenciesByScope = getInjectionsByScope( constructorModels, methodModels, fieldModels );
    }

    public Class getModelClass()
    {
        return modelClass;
    }

    public Iterable<ConstructorModel> getConstructorModels()
    {
        return constructorModels;
    }

    public Iterable<FieldModel> getFieldModels()
    {
        return fieldModels;
    }

    public Iterable<MethodModel> getMethodModels()
    {
        return methodModels;
    }

    public Iterable<InjectionModel> getInjectionsByScope( Class<? extends Annotation> annotationScopeClass )
    {
        Iterable<InjectionModel> dependencies = dependenciesByScope.get( annotationScopeClass );
        if( dependencies == null )
        {
            return Collections.emptySet();
        }
        else
        {
            return dependencies;
        }
    }

    // Object overrides ---------------------------------------------
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        AbstractModel objectModel = (AbstractModel) o;

        return modelClass.equals( objectModel.modelClass );
    }

    public int hashCode()
    {
        return modelClass.hashCode();
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( modelClass.getName() );
        for( FieldModel fieldModel : fieldModels )
        {
            if( fieldModel.getInjectionModel() != null )
            {
                out.println( "    @" + fieldModel.getInjectionModel().getInjectionAnnotationType().getSimpleName() );
            }
        }
        out.close();
        return str.toString();
    }

    protected Map<Class<? extends Annotation>, Set<InjectionModel>> getInjectionsByScope( Iterable<ConstructorModel> constructorModels, Iterable<MethodModel> methodModels, Iterable<FieldModel> fieldModels )
    {
        Map<Class<? extends Annotation>, Set<InjectionModel>> injectionsByScope = new HashMap<Class<? extends Annotation>, Set<InjectionModel>>();
        for( ConstructorModel constructorModel : constructorModels )
        {
            if( constructorModel.hasInjections() )
            {
                for( ParameterModel parameterModel : constructorModel.getParameters() )
                {
                    Class annotationType = parameterModel.getInjectionModel().getInjectionAnnotationType();
                    Set<InjectionModel> dependencyModels = injectionsByScope.get( annotationType );
                    if( dependencyModels == null )
                    {
                        injectionsByScope.put( annotationType, dependencyModels = new HashSet<InjectionModel>() );
                    }

                    dependencyModels.add( parameterModel.getInjectionModel() );
                }
            }
        }

        for( MethodModel dependencyModel : methodModels )
        {
            if( dependencyModel.hasInjections() )
            {
                for( ParameterModel parameterDependencyModel : dependencyModel.getParameterModels() )
                {
                    Class annotationType = parameterDependencyModel.getInjectionModel().getInjectionAnnotationType();
                    Set<InjectionModel> scopeDependencyModels = injectionsByScope.get( annotationType );
                    if( scopeDependencyModels == null )
                    {
                        injectionsByScope.put( annotationType, scopeDependencyModels = new HashSet<InjectionModel>() );
                    }

                    scopeDependencyModels.add( parameterDependencyModel.getInjectionModel() );
                }
            }
        }

        for( FieldModel fieldDependencyModel : fieldModels )
        {
            if( fieldDependencyModel.getInjectionModel() != null )
            {
                Class annotationType = fieldDependencyModel.getInjectionModel().getInjectionAnnotationType();
                Set<InjectionModel> scopeDependencyModels = injectionsByScope.get( annotationType );
                if( scopeDependencyModels == null )
                {
                    injectionsByScope.put( annotationType, scopeDependencyModels = new HashSet<InjectionModel>() );
                }

                scopeDependencyModels.add( fieldDependencyModel.getInjectionModel() );
            }
        }

        return injectionsByScope;
    }


}
