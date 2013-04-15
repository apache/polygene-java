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
import java.util.Map;
import org.qi4j.api.util.Classes;

/**
 * Map ValueType.
 * <p>This handles instances of Map.</p>
 */
public final class MapType
    extends ValueType
{

    private ValueType keyType;
    private ValueType valueType;

    public static boolean isMap( Type type )
    {
        Class<?> cl = Classes.RAW_CLASS.map( type );
        return Map.class.isAssignableFrom( cl );
    }

    public static MapType of( Class<?> keyType, Class<?> valueType )
    {
        return new MapType( Map.class, ValueType.of( keyType ), ValueType.of( valueType ) );
    }

    public MapType( Class<?> type, ValueType keyType, ValueType valueType )
    {
        super( type );
        this.keyType = keyType;
        this.valueType = valueType;
        if( !isMap( type ) )
        {
            throw new IllegalArgumentException( type + " is not a Map." );
        }
    }

    public ValueType keyType()
    {
        return keyType;
    }

    public ValueType valueType()
    {
        return valueType;
    }

    @Override
    public String toString()
    {
        return super.toString() + "<" + keyType + "," + valueType + ">";
    }
}
