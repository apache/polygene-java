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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.spi.property.DefaultValues;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.util.Base64Encoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Map type. This handles instances of Map
 */
public final class MapType
    extends AbstractValueType
{
    private ValueType keyType;
    private ValueType valueType;

    public static boolean isMap( Type type )
    {
        Class cl = Classes.getRawClass( type );
        return cl.equals( Map.class );
    }

    public MapType( TypeName type, ValueType keyType, ValueType valueType )
    {
        super( type );
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public ValueType getKeyType()
    {
        return keyType;
    }

    public ValueType getValueType()
    {
        return valueType;
    }

    @Override
    public String toString()
    {
        return type() + "<" + keyType + "," + valueType + ">";
    }

    public void toJSON( Object value, JSONWriter json )
        throws JSONException
    {
        json.array();

        Map map = (Map) value;
        Set<java.util.Map.Entry> set = map.entrySet();
        for( Map.Entry<Object, Object> entry : set )
        {
            json.object();
            json.key( "key" );
            keyType.toJSON( entry.getKey(), json );
            json.key( "value" );
            valueType.toJSON( entry.getValue(), json );
            json.endObject();
        }

        json.endArray();
    }

    public Object toJSON( Object value )
        throws JSONException
    {
        JSONArray array = new JSONArray();

        Map map = (Map) value;
        Set<java.util.Map.Entry> set = map.entrySet();
        for( Map.Entry<Object, Object> entry : set )
        {
            JSONObject entryJson = new JSONObject();
            entryJson.put( "key", keyType.toJSON( entry.getKey() ) );
            entryJson.put( "value", valueType.toJSON( entry.getValue() ) );
            array.put( entryJson );
        }

        return array;
    }

    public Object fromJSON( Object json, Module module )
        throws JSONException
    {
        if( json instanceof String )
        {
            try
            {
                // Legacy handling of serialized maps
                String serializedString = (String) json;
                byte[] bytes = serializedString.getBytes( "UTF-8" );
                bytes = Base64Encoder.decode( bytes );
                ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
                ObjectInputStream oin = new ObjectInputStream( bin );
                Object result = oin.readObject();
                oin.close();

                return result;
            }
            catch( IOException e )
            {
                throw new IllegalStateException( "Could not deserialize value", e );
            }
            catch( ClassNotFoundException e )
            {
                throw new IllegalStateException( "Could not find class for serialized value", e );
            }
        }
        else
        {
            // New array-based handling
            JSONArray array = (JSONArray) json;

            Map<Object, Object> map = (Map<Object, Object>) DefaultValues.getDefaultValue( Map.class );

            for( int i = 0; i < array.length(); i++ )
            {
                JSONObject entry = array.getJSONObject( i );
                Object key = keyType.fromJSON( entry.get( "key" ), module );
                Object value = valueType.fromJSON( entry.get( "value" ), module );
                map.put( key, value );
            }

            return map;
        }
    }
}
