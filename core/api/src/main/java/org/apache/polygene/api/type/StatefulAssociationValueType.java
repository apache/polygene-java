/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.type;

import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.composite.CompositeDescriptor;
import org.apache.polygene.api.composite.StatefulAssociationCompositeDescriptor;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.api.value.ValueComposite;

public abstract class StatefulAssociationValueType<M extends StatefulAssociationCompositeDescriptor & CompositeDescriptor>
    extends ValueType
{
    public static boolean isStatefulAssociationValue( Type type )
    {
        Class<?> rawClass = Classes.RAW_CLASS.apply( type );
        return ValueComposite.class.isAssignableFrom( rawClass ) || EntityComposite.class.isAssignableFrom( rawClass );
    }

    protected final M model;

    protected StatefulAssociationValueType( M model )
    {
        super( model.types().collect( Collectors.toList() ) );
        this.model = model;
    }

    public ModuleDescriptor module()
    {
        return model.module();
    }

    public Stream<? extends PropertyDescriptor> properties()
    {
        return model.state().properties();
    }

    public Stream<? extends AssociationDescriptor> associations()
    {
        return model.state().associations();
    }

    public Stream<? extends AssociationDescriptor> manyAssociations()
    {
        return model.state().manyAssociations();
    }

    public Stream<? extends AssociationDescriptor> namedAssociations()
    {
        return model.state().namedAssociations();
    }
}
