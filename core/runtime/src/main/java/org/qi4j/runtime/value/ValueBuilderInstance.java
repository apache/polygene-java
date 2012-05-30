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

package org.qi4j.runtime.value;

import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.value.NoSuchValueException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.runtime.structure.ModelModule;

import static org.qi4j.functional.Iterables.first;

/**
 * Implementation of ValueBuilder
 */
public final class ValueBuilderInstance<T>
    implements ValueBuilder<T>
{
    private final ModelModule<ValueModel> model;
    private ValueInstance prototypeInstance;

    public ValueBuilderInstance( ModelModule<ValueModel> model, ValueInstance prototypeInstance )
    {
        this.model = model;
        this.prototypeInstance = prototypeInstance;
    }

    public T prototype()
    {
        return prototypeInstance.<T>proxy();
    }

    @Override
    public AssociationStateHolder state()
    {
        return prototypeInstance.state();
    }

    public <K> K prototypeFor( Class<K> mixinType )
    {
        return prototypeInstance.newProxy(mixinType);
    }

    public T newInstance()
        throws ConstructionException
    {
        Class<Composite> valueType = (Class<Composite>) first( prototypeInstance.types() );

        ModelModule<ValueModel> model = this.model.module().findValueModels(valueType);

        if( model == null )
        {
            throw new NoSuchValueException( valueType.getName(), model.module().name() );
        }
        return new ValueBuilderWithPrototype<T>( model, prototype()).newInstance();
    }
}