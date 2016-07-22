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

package org.apache.zest.valueserialization.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.zest.spi.value.Entry;
import org.apache.zest.spi.value.ObjectField;
import org.apache.zest.spi.value.ParserLocation;
import org.apache.zest.spi.value.VDA;

public class JacksonDeserializer extends VDA<JsonParser>
{
    private final JsonFactory jsonFactory = new JsonFactory();

    @Override
    protected JsonParser adaptInput( InputStream input )
        throws Exception
    {
        return jsonFactory.createParser( input );
    }

    @Override
    protected <T> T readPlainValue( Class<T> type, JsonParser jsonParser )
        throws Exception
    {
        JsonToken currentToken = jsonParser.nextValue();
        if( type.equals( Boolean.class ) || type.equals( Boolean.TYPE ) )
        {
            if( currentToken == JsonToken.VALUE_TRUE )
            {
                return (T) Boolean.TRUE;
            }
            if( currentToken == JsonToken.VALUE_FALSE )
            {
                return (T) Boolean.FALSE;
            }
            return (T) Boolean.valueOf( (String) jsonParser.getCurrentValue() );
        }
        if( type.equals( Byte.class ) || type.equals( Byte.TYPE ) )
        {
            if( currentToken == JsonToken.VALUE_NUMBER_INT ||
                currentToken == JsonToken.VALUE_NUMBER_FLOAT )
            {
                return (T) Byte.valueOf( (Byte) jsonParser.getCurrentValue() );
            }
            return (T) Byte.valueOf( (String) jsonParser.getCurrentValue() );
        }
        if( type.equals( Character.class ) || type.equals( Character.TYPE ) )
        {
            return (T) Character.valueOf( ( (String) jsonParser.getCurrentValue() ).charAt( 0 ) );
        }
        if( type.equals( Short.class ) || type.equals( Short.TYPE ) )
        {
            if( currentToken == JsonToken.VALUE_NUMBER_INT ||
                currentToken == JsonToken.VALUE_NUMBER_FLOAT )
            {
                return (T) Short.valueOf( (Byte) jsonParser.getCurrentValue() );
            }
            return (T) Short.valueOf( (String) jsonParser.getCurrentValue() );
        }
        if( type.equals( Integer.class ) || type.equals( Integer.TYPE ) )
        {
            if( currentToken == JsonToken.VALUE_NUMBER_INT ||
                currentToken == JsonToken.VALUE_NUMBER_FLOAT )
            {
                return (T) Integer.valueOf( (Byte) jsonParser.getCurrentValue() );
            }
            return (T) Integer.valueOf( (String) jsonParser.getCurrentValue() );
        }
        if( type.equals( Long.class ) || type.equals( Long.TYPE ) )
        {
            if( currentToken == JsonToken.VALUE_NUMBER_INT ||
                currentToken == JsonToken.VALUE_NUMBER_FLOAT )
            {
                return (T) Long.valueOf( (Byte) jsonParser.getCurrentValue() );
            }
            return (T) Long.valueOf( (String) jsonParser.getCurrentValue() );
        }
        if( type.equals( Float.class ) || type.equals( Float.TYPE ) )
        {
            if( currentToken == JsonToken.VALUE_NUMBER_INT ||
                currentToken == JsonToken.VALUE_NUMBER_FLOAT )
            {
                return (T) Float.valueOf( (Byte) jsonParser.getCurrentValue() );
            }
            return (T) Float.valueOf( (String) jsonParser.getCurrentValue() );
        }
        if( type.equals( Double.class ) || type.equals( Double.TYPE ) )
        {
            if( currentToken == JsonToken.VALUE_NUMBER_INT ||
                currentToken == JsonToken.VALUE_NUMBER_FLOAT )
            {
                return (T) Double.valueOf( (Byte) jsonParser.getCurrentValue() );
            }
            return (T) Double.valueOf( (String) jsonParser.getCurrentValue() );
        }
        throw new InternalError( "Please contact dev@zest.apache.org." );
    }

    protected <T> T readObjectValue( Type tType, JsonParser jsonParser )
        throws Exception
    {
        JsonToken currentToken = jsonParser.nextToken();
        Map<Object, Object> entries = new HashMap<>();
        if( currentToken == JsonToken.START_OBJECT )
        {
            while( currentToken != JsonToken.END_OBJECT )
            {
                String fieldName = jsonParser.nextFieldName();
                Class<?> fieldType = fieldTypeOf( tType, fieldName );
                Object fieldValue = readObject( fieldType, jsonParser );
                entries.put( fieldName, fieldValue );
                currentToken = jsonParser.nextToken();
            }
            return createObject( tType, entries );
        }
        return null;
    }

    @Override
    protected <T> T readArrayValue( Class<T> type, JsonParser jsonParser )
        throws Exception
    {
        JsonToken currentToken = jsonParser.nextToken();
        List<Object> entries = new ArrayList<>();
        if( currentToken == JsonToken.START_ARRAY )
        {
            while( currentToken != JsonToken.END_ARRAY )
            {
                Object fieldValue = readObject( type.getComponentType(), jsonParser );
                entries.add( fieldValue );
                currentToken = jsonParser.nextToken();
            }
            return createArray( type, entries );
        }
        return null;
    }

    @Override
    protected <K, V> Map<K, V> readMapValue( Type mapType, JsonParser jsonParser )
        throws Exception
    {
        Type keyType;
        Type valueType;
        if( mapType instanceof ParameterizedType )
        {
            keyType = ( (ParameterizedType) mapType ).getActualTypeArguments()[ 0 ];
            valueType = ( (ParameterizedType) mapType ).getActualTypeArguments()[ 1 ];
        }
        else
        {
            keyType = Object.class;
            valueType = Object.class;
        }

        JsonToken currentToken = jsonParser.nextToken();
        Map<Object, Object> entries = new HashMap<>();
        if( currentToken == JsonToken.START_OBJECT )
        {
            while( currentToken != JsonToken.END_OBJECT )
            {
                Object fieldKey = readObject( keyType, jsonParser );
                Object fieldValue = readObject( valueType, jsonParser );
                entries.put( fieldKey, fieldValue );
                currentToken = jsonParser.nextToken();
            }
            return createObject(mapType, entries);
        }
        return null;
    }

    @Override
    protected <T> T readListValue( Type listType, JsonParser jsonParser )
        throws Exception
    {
        return null;
    }

    @Override
    protected <T> T readEnumValue( Class<T> type, JsonParser jsonParser )
        throws Exception
    {
        return null;
    }

    @Override
    protected ObjectField nextField( JsonParser jsonParser )
        throws Exception
    {
        return null;
    }

    @Override
    protected Entry nextEntry( JsonParser jsonParser )
        throws Exception
    {
        return null;
    }

    @Override
    protected Object nextValue( JsonParser jsonParser )
        throws Exception
    {
        return null;
    }

    @Override
    public ParserLocation location( JsonParser jsonParser )
    {
        return null;
    }
}
