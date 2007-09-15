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
package org.qi4j.runtime.resolution;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.model.ObjectModel;

/**
 * Base class for object model resolutions. Resolutions are models resolved in a runtime environment
 */
public class ObjectResolution<T>
{
    // Attributes ----------------------------------------------------
    private ObjectModel<T> objectModel;

    // Dependencies
    private Iterable<ConstructorDependencyResolution> constructorDependencies;
    private Iterable<FieldDependencyResolution> fieldDependencies;
    private Iterable<MethodDependencyResolution> methodDependencies;

    // Constructors --------------------------------------------------
    public ObjectResolution( ObjectModel<T> objectModel, Iterable<ConstructorDependencyResolution> constructorDependencies, Iterable<FieldDependencyResolution> fieldDependencies, Iterable<MethodDependencyResolution> methodDependencies )
    {
        NullArgumentException.validateNotNull( "objectModel", objectModel );

        this.constructorDependencies = constructorDependencies;
        this.fieldDependencies = fieldDependencies;
        this.methodDependencies = methodDependencies;
        this.objectModel = objectModel;
    }

    // Public -------------------------------------------------------
    public ObjectModel<T> getObjectModel()
    {
        return objectModel;
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

        ObjectResolution objectModel = (ObjectResolution) o;

        return this.objectModel.equals( objectModel.objectModel );
    }

    public int hashCode()
    {
        return objectModel.hashCode();
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( objectModel.getModelClass().getName() );
        for( FieldDependencyResolution fieldDependency : fieldDependencies )
        {
            out.println( "    @" + fieldDependency.getFieldDependency().getKey().getAnnotationType().getSimpleName() + " " + fieldDependency.getFieldDependency().getField().getName() );
        }
        out.close();
        return str.toString();
    }
}