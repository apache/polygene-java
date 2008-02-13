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
import java.lang.reflect.Type;
import org.qi4j.spi.injection.InjectionModel;

/**
 * TODO
 */
public final class ParameterModel
    implements Serializable
{
    private Type type;
    private ParameterConstraintsModel parameterConstraintsModel; // May be null
    private InjectionModel injectionModel; // May be null

    public ParameterModel( Type type )
    {
        this.type = type;
    }

    public ParameterModel( Type type, ParameterConstraintsModel parameterConstraintsModel, InjectionModel injectionModel )
    {
        this.type = type;
        this.parameterConstraintsModel = parameterConstraintsModel;
        this.injectionModel = injectionModel;
    }

    public Type getType()
    {
        return type;
    }

    public ParameterConstraintsModel getParameterConstraintModel()
    {
        return parameterConstraintsModel;
    }

    public InjectionModel getInjectionModel()
    {
        return injectionModel;
    }
}
