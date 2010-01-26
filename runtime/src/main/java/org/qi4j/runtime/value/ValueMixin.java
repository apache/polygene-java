/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import org.json.JSONException;
import org.json.JSONStringer;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.property.ValueType;

/**
 * Implementation of Value
 */

public class ValueMixin
    implements Value
{
    @This
    private Value thisValue;

    @State
    private StateHolder state;

    public StateHolder state()
    {
        return state;
    }

    public <T> ValueBuilder<T> buildWith()
    {
        ValueInstance valueInstance = ValueInstance.getValueInstance( (ValueComposite) thisValue );
        Class<Composite> valueType = (Class<Composite>) valueInstance.type();
        return (ValueBuilder<T>) valueInstance.module()
            .valueBuilderFactory()
            .newValueBuilder( valueType )
            .withPrototype( (Composite) thisValue );
    }

    public String toJSON()
    {
        ValueInstance valueInstance = ValueInstance.getValueInstance( (ValueComposite) thisValue );

        ValueType valueType = ( (ValueModel) valueInstance.compositeModel() ).valueType();
        JSONStringer json = new JSONStringer();
        try
        {
            valueType.toJSON( thisValue, json );
        }
        catch( JSONException e )
        {
            throw new IllegalStateException( "Could not JSON serialize value", e );
        }
        return json.toString();
    }
}
