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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see MixinModel
 * @see ModifierModel
 */
public abstract class FragmentModel<T>
{
    // Attributes ----------------------------------------------------
    private Class<T> fragmentClass;

    // Dependencies
    private Iterable<ConstructorDependency> constructorDependencies;
    private Iterable<FieldDependency> fieldDependencies;
    private Iterable<MethodDependency> methodDependencies;
    protected Class appliesTo;

    // Constructors --------------------------------------------------
    public FragmentModel( Class<T> fragmentClass, Iterable<ConstructorDependency> constructorDependencies, Iterable<FieldDependency> fieldDependencies, Iterable<MethodDependency> methodDependencies, Class appliesTo )
    {
        NullArgumentException.validateNotNull( "fragmentClass", fragmentClass );
        this.constructorDependencies = constructorDependencies;
        this.fieldDependencies = fieldDependencies;
        this.methodDependencies = methodDependencies;
        this.fragmentClass = fragmentClass;
        this.appliesTo = appliesTo;
    }

    // Public -------------------------------------------------------
    public Class<T> getFragmentClass()
    {
        return fragmentClass;
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

    public Class getAppliesTo()
    {
        return appliesTo;
    }

    public boolean isAbstract()
    {
        return Modifier.isAbstract( fragmentClass.getModifiers() );
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( fragmentClass );
    }

    public Iterable<Dependency> getDependenciesByScope( Class<? extends Annotation> annotationScopeClass )
    {
        List<Dependency> scopeDependencies = new ArrayList<Dependency>();

        for( ConstructorDependency constructorDependency : constructorDependencies )
        {
            for( ParameterDependency parameterDependency : constructorDependency.getParameterDependencies() )
            {
                if( parameterDependency.getKey().getAnnotationType().equals( annotationScopeClass ) )
                {
                    scopeDependencies.add( parameterDependency );
                }
            }
        }

        for( FieldDependency fieldDependency : fieldDependencies )
        {
            if( fieldDependency.getKey().getAnnotationType().equals( annotationScopeClass ) )
            {
                scopeDependencies.add( fieldDependency );
            }
        }

        return scopeDependencies;
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

        FragmentModel fragmentModel = (FragmentModel) o;

        return fragmentClass.equals( fragmentModel.fragmentClass );
    }

    public int hashCode()
    {
        return fragmentClass.hashCode();
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( fragmentClass.getName() );
        for( FieldDependency fieldDependency : fieldDependencies )
        {
            out.println( "    @" + fieldDependency.getKey().getAnnotationType().getSimpleName() + " " + fieldDependency.getField().getName() );
        }
        out.close();
        return str.toString();
    }
}
