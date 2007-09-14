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
import org.qi4j.api.ConstructorDependencyResolution;
import org.qi4j.api.FieldDependencyResolution;
import org.qi4j.api.MethodDependencyResolution;

/**
 * Base class for fragment model instances. Instances are models resolved in a runtime environment
 *
 * @see MixinResolution
 * @see ModifierResolution
 */
public abstract class FragmentResolution<T>
{
    // Attributes ----------------------------------------------------
    private FragmentModel<T> fragmentModel;

    // Dependencies
    private Iterable<ConstructorDependencyResolution> constructorDependencies;
    private Iterable<FieldDependencyResolution> fieldDependencies;
    private Iterable<MethodDependencyResolution> methodDependencies;

    // Constructors --------------------------------------------------
    public FragmentResolution( FragmentModel<T> fragmentModel, Iterable<ConstructorDependencyResolution> constructorDependencies, Iterable<FieldDependencyResolution> fieldDependencies, Iterable<MethodDependencyResolution> methodDependencies )
    {
        NullArgumentException.validateNotNull( "fragmentModel", fragmentModel );
        this.constructorDependencies = constructorDependencies;
        this.fieldDependencies = fieldDependencies;
        this.methodDependencies = methodDependencies;
        this.fragmentModel = fragmentModel;
    }

    // Public -------------------------------------------------------
    public FragmentModel<T> getFragmentModel()
    {
        return fragmentModel;
    }

    public Iterable<ConstructorDependencyResolution> getConstructorResolutions()
    {
        return constructorDependencies;
    }

    public Iterable<FieldDependencyResolution> getFieldResolutions()
    {
        return fieldDependencies;
    }

    public Iterable<MethodDependencyResolution> getMethodResolutions()
    {
        return methodDependencies;
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

        FragmentResolution fragmentModel = (FragmentResolution) o;

        return this.fragmentModel.equals( fragmentModel.fragmentModel );
    }

    public int hashCode()
    {
        return fragmentModel.hashCode();
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( fragmentModel.getFragmentClass().getName() );
        for( FieldDependencyResolution fieldDependency : fieldDependencies )
        {
            out.println( "    @" + fieldDependency.getFieldDependency().getKey().getAnnotationType().getSimpleName() + " " + fieldDependency.getFieldDependency().getField().getName() );
        }
        out.close();
        return str.toString();
    }
}