/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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
package org.apache.zest.runtime.value;

import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.common.ConstructionException;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.value.NoSuchValueException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.runtime.composite.StateResolver;
import org.apache.zest.runtime.structure.ModuleInstance;
import org.apache.zest.spi.module.ModelModule;

/**
 * Implementation of ValueBuilder
 */
public final class ValueBuilderInstance<T>
    implements ValueBuilder<T>
{

    private final ModuleInstance currentModule;
    private final ValueInstance prototypeInstance;

    public ValueBuilderInstance( ModelModule<ValueModel> compositeModelModule,
                                 ModuleInstance currentModule,
                                 StateResolver stateResolver
    )
    {
        ValueStateInstance state = new ValueStateInstance( compositeModelModule, currentModule, stateResolver );
        prototypeInstance = compositeModelModule.model().newValueInstance( compositeModelModule.module(), state );
        prototypeInstance.prepareToBuild();
        this.currentModule = currentModule;
    }

    @Override
    public T prototype()
    {
        return prototypeInstance.<T>proxy();
    }

    @Override
    public AssociationStateHolder state()
    {
        return prototypeInstance.state();
    }

    @Override
    public <K> K prototypeFor( Class<K> mixinType )
    {
        return prototypeInstance.newProxy( mixinType );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public T newInstance()
        throws ConstructionException
    {
        Class<Composite> valueType = (Class<Composite>) prototypeInstance.types().findFirst().orElse( null );

        ModelModule<ValueModel> valueModel = currentModule.typeLookup().lookupValueModel( valueType );

        if( valueModel == null )
        {
            throw new NoSuchValueException( valueType.getName(), currentModule.name() );
        }
        return new ValueBuilderWithPrototype<>( valueModel, currentModule, prototype() ).newInstance();
    }
}
