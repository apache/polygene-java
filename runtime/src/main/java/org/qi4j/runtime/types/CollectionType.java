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

package org.qi4j.runtime.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.spi.property.ValueType;

/**
 * Collection type. This handles Collection, List and Set types
 */
public final class CollectionType
    extends AbstractValueType
{
    public static boolean isCollection( Type type )
    {
        Class cl = Classes.getRawClass( type );
        return cl.equals( Collection.class ) || cl.equals( List.class ) || cl.equals( Set.class );
    }

    private ValueType collectedType;

    public CollectionType( TypeName type, ValueType collectedType )
    {
        super( type );
        this.collectedType = collectedType;
    }

    public ValueType collectedType()
    {
        return collectedType;
    }

    @Override
    public String toString()
    {
        return type() + "<" + collectedType + ">";
    }

    public void toJSON( Object value, JSONWriter json )
        throws JSONException
    {
        json.array();

        Collection collection = (Collection) value;
        for( Object collectionValue : collection )
        {
            collectedType.toJSON( collectionValue, json );
        }

        json.endArray();
    }

    public Object toJSON( Object value )
        throws JSONException
    {
        JSONArray array = new JSONArray();

        Collection collection = (Collection) value;
        int idx = 0;
        for( Object collectionValue : collection )
        {
            array.put( idx++, collectedType.toJSON( collectionValue ) );
        }

        return array;
    }

    public Object fromJSON( Object json, Module module )
        throws JSONException
    {
        JSONArray array = (JSONArray) json;

        Collection<Object> coll;
        if( type().isClass( List.class ) )
        {
            coll = new ArrayList<Object>();
        }
        else
        {
            coll = new LinkedHashSet<Object>();
        }

        for( int i = 0; i < array.length(); i++ )
        {
            Object value = array.get( i );
            coll.add( collectedType.fromJSON( value, module ) );
        }

        return coll;
    }
}
