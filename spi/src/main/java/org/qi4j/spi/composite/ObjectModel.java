/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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
import org.qi4j.spi.dependency.InjectionModel;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see MixinModel
 * @see ConcernModel
 */
public class ObjectModel
    implements Serializable
{
    private Class modelClass;

    private Iterable<ConstructorModel> constructorModels;
    private Iterable<FieldModel> fieldModels;
    private Iterable<MethodModel> methodModels;
    private Map<Class<? extends Annotation>, Set<InjectionModel>> dependenciesByScope;

    public ObjectModel( Class modelClass, Iterable<ConstructorModel> constructorModels, Iterable<FieldModel> fieldModels, Iterable<MethodModel> methodModels )
    {
        this.modelClass = modelClass;
        this.constructorModels = constructorModels;
        this.fieldModels = fieldModels;
        this.methodModels = methodModels;
        this.dependenciesByScope = getDependenciesByScope( constructorModels, methodModels, fieldModels );
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

        ObjectModel objectModel = (ObjectModel) o;

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

    protected Map<Class<? extends Annotation>, Set<InjectionModel>> getDependenciesByScope( Iterable<ConstructorModel> constructorDependencies, Iterable<MethodModel> methodDependencies, Iterable<FieldModel> fieldDependencies )
    {
        Map<Class<? extends Annotation>, Set<InjectionModel>> dependenciesByScope = new HashMap<Class<? extends Annotation>, Set<InjectionModel>>();
        for( ConstructorModel constructorDependencyModel : constructorDependencies )
        {
            for( ParameterModel parameterDependencyModel : constructorDependencyModel.getParameters() )
            {
                Class annotationType = parameterDependencyModel.getInjectionModel().getInjectionAnnotationType();
                Set<InjectionModel> scopeDependencyModels = dependenciesByScope.get( annotationType );
                if( scopeDependencyModels == null )
                {
                    dependenciesByScope.put( annotationType, scopeDependencyModels = new HashSet<InjectionModel>() );
                }

                scopeDependencyModels.add( parameterDependencyModel.getInjectionModel() );
            }
        }

        for( MethodModel dependencyModel : methodDependencies )
        {
            if( dependencyModel.hasInjections() )
            {
                for( ParameterModel parameterDependencyModel : dependencyModel.getParameterModels() )
                {
                    Class annotationType = parameterDependencyModel.getInjectionModel().getInjectionAnnotationType();
                    Set<InjectionModel> scopeDependencyModels = dependenciesByScope.get( annotationType );
                    if( scopeDependencyModels == null )
                    {
                        dependenciesByScope.put( annotationType, scopeDependencyModels = new HashSet<InjectionModel>() );
                    }

                    scopeDependencyModels.add( parameterDependencyModel.getInjectionModel() );
                }
            }
        }

        for( FieldModel fieldDependencyModel : fieldDependencies )
        {
            if( fieldDependencyModel.getInjectionModel() != null )
            {
                Class annotationType = fieldDependencyModel.getInjectionModel().getInjectionAnnotationType();
                Set<InjectionModel> scopeDependencyModels = dependenciesByScope.get( annotationType );
                if( scopeDependencyModels == null )
                {
                    dependenciesByScope.put( annotationType, scopeDependencyModels = new HashSet<InjectionModel>() );
                }

                scopeDependencyModels.add( fieldDependencyModel.getInjectionModel() );
            }
        }

        return dependenciesByScope;
    }


}