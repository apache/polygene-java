/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for object model bindings. Bindings are resolutions whose injections have been bound to injection providers.
 */
public class ObjectBinding
{
    private ObjectResolution objectResolution;

    private ConstructorBinding constructorBinding;
    private Iterable<FieldBinding> fieldBindings;
    private Iterable<MethodBinding> methodBindings;
    private Iterable<MethodBinding> injectedMethodBindings;

    public ObjectBinding( ObjectResolution objectResolution, ConstructorBinding constructorBinding, Iterable<FieldBinding> fieldBindings, Iterable<MethodBinding> methodBindings )
    {
        this.objectResolution = objectResolution;
        this.constructorBinding = constructorBinding;
        this.fieldBindings = fieldBindings;
        this.methodBindings = methodBindings;

        List<MethodBinding> injectedMethodBindings = new ArrayList<MethodBinding>();
        for( MethodBinding methodBinding : methodBindings )
        {
            if( methodBinding.getMethodResolution().getMethodModel().hasInjections() )
            {
                injectedMethodBindings.add( methodBinding );
            }
        }
        this.injectedMethodBindings = injectedMethodBindings;
    }

    public ObjectResolution getObjectResolution()
    {
        return objectResolution;
    }

    public ConstructorBinding getConstructorBinding()
    {
        return constructorBinding;
    }

    public Iterable<FieldBinding> getFieldBindings()
    {
        return fieldBindings;
    }

    public Iterable<MethodBinding> getMethodBindings()
    {
        return methodBindings;
    }

    public Iterable<MethodBinding> getInjectedMethodsBindings()
    {
        return injectedMethodBindings;
    }

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

        ObjectBinding objectModel = (ObjectBinding) o;

        return this.objectResolution.equals( objectModel.objectResolution );
    }

    public int hashCode()
    {
        return objectResolution.hashCode();
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( objectResolution.getObjectModel().getModelClass().getName() );
        for( FieldBinding fieldBinding : fieldBindings )
        {
            out.println( "    @" + fieldBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().getInjectionAnnotationType().getSimpleName() );
        }
        out.close();
        return str.toString();
    }
}
