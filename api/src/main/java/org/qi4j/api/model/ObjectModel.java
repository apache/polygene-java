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
package org.qi4j.api.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see org.qi4j.api.model.MixinModel
 * @see org.qi4j.api.model.AssertionModel
 */
public class ObjectModel<T>
{
    private Class<T> modelClass;

    // Dependencies
    private Iterable<ConstructorDependency> constructorDependencies;
    private Iterable<FieldDependency> fieldDependencies;
    private Iterable<MethodDependency> methodDependencies;
    private Map<Class<? extends Annotation>, Set<Dependency>> dependenciesByScope;

    public ObjectModel( Class<T> modelClass, Iterable<ConstructorDependency> constructorDependencies, Iterable<FieldDependency> fieldDependencies, Iterable<MethodDependency> methodDependencies )
    {
        NullArgumentException.validateNotNull( "Model class", modelClass );

        this.modelClass = modelClass;
        this.constructorDependencies = constructorDependencies;
        this.fieldDependencies = fieldDependencies;
        this.methodDependencies = methodDependencies;
        this.dependenciesByScope = getDependenciesByScope( constructorDependencies, methodDependencies, fieldDependencies );
    }

    public Class<T> getModelClass()
    {
        return modelClass;
    }

    public Iterable<ConstructorDependency> getConstructorDependencies()
    {
        return constructorDependencies;
    }

    public Iterable<FieldDependency> getFieldDependencies()
    {
        return fieldDependencies;
    }

    public Iterable<MethodDependency> getMethodDependencies()
    {
        return methodDependencies;
    }

    public Iterable<Dependency> getDependenciesByScope( Class<? extends Annotation> annotationScopeClass )
    {
        Iterable<Dependency> dependencies = dependenciesByScope.get( annotationScopeClass );
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
        for( FieldDependency fieldDependency : fieldDependencies )
        {
            out.println( "    @" + fieldDependency.getKey().getAnnotationType().getSimpleName() + " " + fieldDependency.getField().getName() );
        }
        out.close();
        return str.toString();
    }

    protected Map<Class<? extends Annotation>, Set<Dependency>> getDependenciesByScope( Iterable<ConstructorDependency> constructorDependencies, Iterable<MethodDependency> methodDependencies, Iterable<FieldDependency> fieldDependencies )
    {
        Map<Class<? extends Annotation>, Set<Dependency>> dependenciesByScope = new HashMap<Class<? extends Annotation>, Set<Dependency>>();
        for( ConstructorDependency constructorDependency : constructorDependencies )
        {
            for( ParameterDependency parameterDependency : constructorDependency.getParameterDependencies() )
            {
                Class annotationType = parameterDependency.getKey().getAnnotationType();
                Set<Dependency> scopeDependencies = dependenciesByScope.get( annotationType );
                if( scopeDependencies == null )
                {
                    dependenciesByScope.put( annotationType, scopeDependencies = new HashSet<Dependency>() );
                }

                scopeDependencies.add( parameterDependency );
            }
        }

        for( MethodDependency constructorDependency : methodDependencies )
        {
            for( ParameterDependency parameterDependency : constructorDependency.getParameterDependencies() )
            {
                Class annotationType = parameterDependency.getKey().getAnnotationType();
                Set<Dependency> scopeDependencies = dependenciesByScope.get( annotationType );
                if( scopeDependencies == null )
                {
                    dependenciesByScope.put( annotationType, scopeDependencies = new HashSet<Dependency>() );
                }

                scopeDependencies.add( parameterDependency );
            }
        }

        for( FieldDependency fieldDependency : fieldDependencies )
        {
            Class annotationType = fieldDependency.getKey().getAnnotationType();
            Set<Dependency> scopeDependencies = dependenciesByScope.get( annotationType );
            if( scopeDependencies == null )
            {
                dependenciesByScope.put( annotationType, scopeDependencies = new HashSet<Dependency>() );
            }

            scopeDependencies.add( fieldDependency );
        }

        return dependenciesByScope;
    }


}