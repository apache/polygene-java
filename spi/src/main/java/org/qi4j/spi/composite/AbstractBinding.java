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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for object model bindings. Bindings are resolutions whose injections have been bound to injection providers.
 */
public abstract class AbstractBinding
{
    private AbstractResolution abstractResolution;

    private ConstructorBinding constructorBinding;
    private Iterable<FieldBinding> fieldBindings;
    private Iterable<MethodBinding> methodBindings;
    private Iterable<MethodBinding> injectedMethodBindings;

    protected AbstractBinding( AbstractResolution abstractResolution, ConstructorBinding constructorBinding, Iterable<FieldBinding> fieldBindings, Iterable<MethodBinding> methodBindings )
    {
        this.abstractResolution = abstractResolution;
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

    public AbstractResolution getAbstractResolution()
    {
        return abstractResolution;
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

        AbstractBinding objectModel = (AbstractBinding) o;

        return this.abstractResolution.equals( objectModel.abstractResolution );
    }

    public int hashCode()
    {
        return abstractResolution.hashCode();
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( abstractResolution.getAbstractModel().getModelClass().getName() );
        for( FieldBinding fieldBinding : fieldBindings )
        {
            out.println( "    @" + fieldBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().getInjectionAnnotationType().getSimpleName() );
        }
        out.close();
        return str.toString();
    }
}
