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

package org.qi4j.spi.property;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.spi.util.Base64Encoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Map type. This handles instances of Map
 */
public final class MapType
    extends ValueType
{
    private ValueType keyType;
    private ValueType valueType;

    public static boolean isMap( Type type )
    {
        Class cl = Classes.RAW_CLASS.map( type );
        return Map.class.isAssignableFrom( cl );
    }

    public MapType( Class<?> type, ValueType keyType, ValueType valueType )
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
        return type().getName() + "<" + keyType + "," + valueType + ">";
    }
}
