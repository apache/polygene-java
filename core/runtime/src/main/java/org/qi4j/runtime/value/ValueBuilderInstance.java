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

import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.runtime.association.AssociationInfo;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.ManyAssociationInstance;
import org.qi4j.runtime.property.PropertyInfo;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.structure.ModelModule;

/**
 * Implementation of ValueBuilder
 */
public final class ValueBuilderInstance<T>
    implements ValueBuilder<T>
{
    private final ModelModule<ValueModel> model;
    private ValueInstance prototypeInstance;

    public ValueBuilderInstance( ModelModule<ValueModel> model, ValueInstance prototypeInstance)
    {
        this.model = model;
        this.prototypeInstance = prototypeInstance;
    }

    public T prototype()
    {
        if (prototypeInstance == null)
            throw new IllegalStateException( "ValueBuilder instances cannot be reused" );

        return prototypeInstance.<T>proxy();
    }

    @Override
    public AssociationStateHolder state()
    {
        if (prototypeInstance == null)
            throw new IllegalStateException( "ValueBuilder instances cannot be reused" );

        return prototypeInstance.state();
    }

    public <K> K prototypeFor( Class<K> mixinType )
    {
        if (prototypeInstance == null)
            throw new IllegalStateException( "ValueBuilder instances cannot be reused" );

        return prototypeInstance.newProxy( mixinType );
    }

    public T newInstance()
        throws ConstructionException
    {
        if (prototypeInstance == null)
            throw new IllegalStateException( "ValueBuilder instances cannot be reused" );

        // Set correct info's (immutable) on the state
        prototypeInstance.prepareBuilderState();

        // Check that it is valid
        model.model().checkConstraints( prototypeInstance.state() );

        try
        {
            return prototypeInstance.<T>proxy();
        } finally
        {
            // Invalidate builder
            prototypeInstance = null;
        }
    }
}