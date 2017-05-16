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
import java.util.Objects;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.api.value.ValueDescriptor;

/**
 * ValueComposite ValueType.
 */
public final class ValueCompositeType extends StatefulAssociationValueType<ValueDescriptor>
{
    public static ValueCompositeType of( ValueDescriptor model )
    {
        return new ValueCompositeType( model );
    }

    public static boolean isValueComposite( Type type )
    {
        return ValueComposite.class.isAssignableFrom( Classes.RAW_CLASS.apply( type ) );
    }

    public ValueCompositeType( ValueDescriptor model )
    {
        super( model );
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o ) { return true; }
        if( o == null || getClass() != o.getClass() ) { return false; }
        if( !super.equals( o ) ) { return false; }
        ValueCompositeType that = (ValueCompositeType) o;
        return Objects.equals( model, that.model );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( super.hashCode(), model );
    }
}
