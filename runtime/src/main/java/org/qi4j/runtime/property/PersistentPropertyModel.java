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

package org.qi4j.runtime.property;

import org.json.JSONException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.json.JSONWriterSerializer;
import org.qi4j.api.property.PersistentPropertyDescriptor;
import org.qi4j.runtime.composite.ValueConstraintsInstance;

import java.lang.reflect.AccessibleObject;

/**
 * JAVADOC
 */
public abstract class PersistentPropertyModel
    extends AbstractPropertyModel
    implements PersistentPropertyDescriptor
{
    private final boolean queryable;

    public PersistentPropertyModel( AccessibleObject accessor, boolean immutable,
                                    ValueConstraintsInstance constraints, MetaInfo metaInfo, Object initialValue
    )
    {
        super( accessor, immutable, constraints, metaInfo, initialValue );

        final Queryable queryable = accessor.getAnnotation( Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    public boolean queryable()
    {
        return queryable;
    }

    public String toJSON( Object value )
    {
        if( value == null )
        {
            return "null";
        }

        try
        {
            JSONWriterSerializer valueJSONWriterSerializer = new JSONWriterSerializer();
            valueJSONWriterSerializer.serialize( value, valueType() );
            return valueJSONWriterSerializer.getJSON().toString();
        }
        catch( JSONException e )
        {
            throw new IllegalStateException( "Could not serialize value to JSON", e );
        }
    }
}
