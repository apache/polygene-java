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
import java.io.StringWriter;

/**
 * Base class for object model resolutions. Resolutions are models resolved in a runtime environment
 */
public class ObjectResolution
{
    private ObjectModel objectModel;

    private Iterable<ConstructorResolution> constructorResolutions;
    private Iterable<FieldResolution> fieldResolutions;
    private Iterable<MethodResolution> methodResolutions;

    public ObjectResolution( ObjectModel objectModel, Iterable<ConstructorResolution> constructorResolutions, Iterable<FieldResolution> fieldResolutions, Iterable<MethodResolution> methodResolutions )
    {
        this.constructorResolutions = constructorResolutions;
        this.fieldResolutions = fieldResolutions;
        this.methodResolutions = methodResolutions;
        this.objectModel = objectModel;
    }

    // Public -------------------------------------------------------
    public ObjectModel getObjectModel()
    {
        return objectModel;
    }

    public Iterable<ConstructorResolution> getConstructorResolutions()
    {
        return constructorResolutions;
    }

    public Iterable<FieldResolution> getFieldResolutions()
    {
        return fieldResolutions;
    }

    public Iterable<MethodResolution> getMethodResolutions()
    {
        return methodResolutions;
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
        for( FieldResolution fieldResolution : fieldResolutions )
        {
            out.println( "    @" + fieldResolution.getInjectionResolution().getInjectionModel().getInjectionAnnotationType().getSimpleName() );
        }
        out.close();
        return str.toString();
    }
}