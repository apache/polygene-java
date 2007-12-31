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

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * TODO
 */
public final class MethodModel
    implements Serializable
{
    private Method method;
    private boolean hasInjections;
    private Iterable<ParameterModel> parameterModels;

    public MethodModel( Method method, Iterable<ParameterModel> parameters, boolean hasInjections )
    {
        this.hasInjections = hasInjections;
        this.method = method;
        this.parameterModels = parameters;
    }

    public Method getMethod()
    {
        return method;
    }

    public Iterable<ParameterModel> getParameterModels()
    {
        return parameterModels;
    }

    public boolean hasInjections()
    {
        return hasInjections;
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

        MethodModel that = (MethodModel) o;

        if( !method.equals( that.method ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return method.hashCode();
    }


    @Override public String toString()
    {
        return method.toGenericString();
    }
}
