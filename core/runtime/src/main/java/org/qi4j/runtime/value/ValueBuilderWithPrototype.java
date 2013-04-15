/*
 * Copyright 2007, Rickard Öberg.
 * Copyright 2009, Niclas Hedhman.
 * Copyright 2012, Kent Sølvsten.
 * Copyright 2013, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.value;

import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.runtime.structure.ModelModule;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * Implementation of ValueBuilder with a prototype supplied
 */
public class ValueBuilderWithPrototype<T>
    implements ValueBuilder<T>
{
    private ValueInstance prototypeInstance;
    private final ValueModel valueModel;

    public ValueBuilderWithPrototype(ModelModule<ValueModel> compositeModelModule, ModuleInstance currentModule, T prototype)
    {
        valueModel = compositeModelModule.model();
        // Use serialization-deserialization to make a copy of the prototype
        final Object value;
        try
        {
            // @TODO there is probably a more efficient way to do this
            ValueSerialization valueSerialization = currentModule.valueSerialization();
            String serialized = valueSerialization.serialize( prototype );
            value = valueSerialization.deserialize( valueModel.valueType(), serialized);
        }
        catch( ValueSerializationException e )
        {
            throw new IllegalStateException( "Could not serialize-copy Value", e );
        }

        ValueInstance valueInstance = ValueInstance.valueInstanceOf( (ValueComposite) value );
        valueInstance.prepareToBuild();
        this.prototypeInstance = valueInstance;
    }

    @Override
    public T prototype()
    {
        verifyUnderConstruction();
        return prototypeInstance.<T>proxy();
    }

    @Override
    public AssociationStateHolder state()
    {
        verifyUnderConstruction();
        return prototypeInstance.state();
    }

    @Override
    public <K> K prototypeFor( Class<K> mixinType )
    {
        verifyUnderConstruction();
        return prototypeInstance.newProxy( mixinType );
    }

    @Override
    public T newInstance()
        throws ConstructionException
    {
        verifyUnderConstruction();

        // Set correct info's (immutable) on the state
        prototypeInstance.prepareBuilderState();

        // Check that it is valid
        valueModel.checkConstraints( prototypeInstance.state() );

        try
        {
            return prototypeInstance.<T>proxy();
        }
        finally
        {
            // Invalidate builder
            prototypeInstance = null;
        }
    }

    private void verifyUnderConstruction()
    {
        if( prototypeInstance == null )
        {
            throw new IllegalStateException( "ValueBuilder instances cannot be reused" );
        }
    }

}
