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
package org.qi4j.api.type;

import java.lang.reflect.Type;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;

/**
 * ValueComposite ValueType.
 */
public final class ValueCompositeType
    extends ValueType
{
    private final ValueDescriptor model;

    public static boolean isValueComposite( Type type )
    {
        return ValueComposite.class.isAssignableFrom( Classes.RAW_CLASS.map( type ) );
    }

    public ValueCompositeType( ValueDescriptor model )
    {
        super( model.types() );
        this.model = model;
    }

    public Iterable<? extends PropertyDescriptor> properties()
    {
        return model.state().properties();
    }

    public Iterable<? extends AssociationDescriptor> associations()
    {
        return model.state().associations();
    }

    public Iterable<? extends AssociationDescriptor> manyAssociations()
    {
        return model.state().manyAssociations();
    }

    public Iterable<? extends AssociationDescriptor> namedAssociations()
    {
        return model.state().namedAssociations();
    }
}
