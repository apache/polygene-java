/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.spi.structure;

import java.util.function.Function;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.spi.module.ModuleSpi;

/**
 * TODO
 */
public class ModelModule<T extends ModelDescriptor>
{

    public static Function<ModelModule<?>, String> toStringFunction = item -> item.model()
                                                                                  .types()
                                                                                  .iterator()
                                                                                  .next()
                                                                                  .getName() + "[" + item.module()
                                                                                  .name() + "]";

    public static <T extends ModelDescriptor> Function<T, ModelModule<T>> modelModuleFunction( final ModuleSpi module )
    {
        return model1 -> new ModelModule<>( module, model1 );
    }

    public static Function<ModelModule<? extends ModelDescriptor>, ModelDescriptor> modelFunction()
    {
        return modelModule -> {
            return modelModule.model();
        };
    }

    private final ModuleSpi module;
    private final T model;

    public ModelModule( ModuleSpi module, T model )
    {
        this.module = module;
        this.model = model;
    }

    public ModuleSpi module()
    {
        return module;
    }

    public T model()
    {
        return model;
    }

    @Override
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

        ModelModule that = (ModelModule) o;

        //noinspection SimplifiableIfStatement
        if( model != null ? !model.equals( that.model ) : that.model != null )
        {
            return false;
        }

        return !( module != null ? !module.equals( that.module ) : that.module != null );
    }

    @Override
    public int hashCode()
    {
        int result = module != null ? module.hashCode() : 0;
        result = 31 * result + ( model != null ? model.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString()
    {
        return module.name() + ":" + model;
    }
}
