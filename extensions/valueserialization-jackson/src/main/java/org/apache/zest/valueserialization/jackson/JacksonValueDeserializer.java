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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.value.ValueSerializationException;
import org.apache.zest.spi.value.ValueDeserializerAdapter;

/**
 * ValueDeserializer reading Values from JSON documents using Jackson.
 */
public class JacksonValueDeserializer
    extends ValueDeserializerAdapter<JsonParser, JsonNode>
{

    private final JsonFactory jsonFactory = new MappingJsonFactory();

    @Override
    protected JsonParser adaptInput( ModuleDescriptor module, InputStream input )
        throws Exception
    {
        return jsonFactory.createParser( input );
    }

    @Override
    protected void onDeserializationEnd( ModuleDescriptor module, ValueType valueType, JsonParser input )
        throws Exception
    {
        input.close();
    }

    @Override
    protected Object readPlainValue( ModuleDescriptor module, JsonParser input )
        throws Exception
    {
        JsonNode jsonNode = input.readValueAsTree();
        if( jsonNode.isArray() || jsonNode.isObject() )
        {
            throw new ValueSerializationException( "Asked for a Value but found an Object or an Array at "
                                                   + input.getCurrentLocation().toString() );
        }
        if( jsonNode.isDouble() )
        {
            return jsonNode.asDouble();
        }
        if( jsonNode.isLong() )
        {
            return jsonNode.asLong();
        }
        if( jsonNode.isInt() )
        {
            return jsonNode.asInt();
        }
        if( jsonNode.isBoolean() )
        {
            return jsonNode.asBoolean();
        }
        if( jsonNode.isNull() )
        {
            return null;
        }
        return jsonNode.asText();
    }

    @Override
    protected <T> Collection<T> readArrayInCollection( ModuleDescriptor module,
                                                       JsonParser input,
                                                       Function<JsonParser, T> deserializer,
                                                       Collection<T> collection
    )
        throws Exception
    {
        JsonToken token = input.getCurrentToken();
        if( token == JsonToken.VALUE_NULL )
        {
            return null;
        }
        if( token != JsonToken.START_ARRAY )
        {
            token = input.nextToken();
        }
        if( token == JsonToken.VALUE_NULL )
        {
            return null;
        }
        if( token != JsonToken.START_ARRAY )
        {
            throw new ValueSerializationException( "Expected an array start at "
                                                   + input.getCurrentLocation().toString() );
        }
        while( input.nextToken() != JsonToken.END_ARRAY )
        {
            T element = deserializer.apply( input );
            collection.add( element );
        }
        return collection;
    }

    @Override
    protected <K, V> Map<K, V> readMapInMap( ModuleDescriptor module,
                                             JsonParser input,
                                             Function<JsonParser, K> keyDeserializer,
                                             Function<JsonParser, V> valueDeserializer,
                                             Map<K, V> map
    )
        throws Exception
    {
        JsonToken token = input.getCurrentToken();
        if( token == JsonToken.VALUE_NULL )
        {
            return null;
        }
        if( token != JsonToken.START_ARRAY )
        {
            token = input.nextToken();
        }
        if( token == JsonToken.VALUE_NULL )
        {
            return null;
        }
        if( token != JsonToken.START_ARRAY )
        {
            throw new ValueSerializationException( "Expected an array start at "
                                                   + input.getCurrentLocation().toString() );
        }
        JsonToken currentToken = input.nextToken();
        while( currentToken != JsonToken.END_ARRAY )
        {
            if( currentToken != JsonToken.START_OBJECT )
            {
                throw new ValueSerializationException( "Expected an object start at "
                                                       + input.getCurrentLocation().toString() );
            }
            currentToken = input.nextToken();
            K key = null;
            V value = null;
            while( currentToken != JsonToken.END_OBJECT )
            {
                String objectKey = input.getCurrentName();
                input.nextToken();
                if( "key".equals( objectKey ) )
                {
                    key = keyDeserializer.apply( input );
                }
                else if( "value".equals( objectKey ) )
                {
                    value = valueDeserializer.apply( input );
                }
                else
                {
                    //input.nextToken();
                    input.skipChildren();
                }
                currentToken = input.nextToken();
            }
            if( key != null )
            {
                map.put( key, value );
            }
            currentToken = input.nextToken();
        }
        return map;
    }

    @Override
    protected ObjectNode readObjectTree( ModuleDescriptor module, JsonParser input )
        throws Exception
    {
        JsonToken token = input.getCurrentToken();
        if( token == JsonToken.VALUE_NULL )
        {
            return null;
        }
        if( token != JsonToken.START_OBJECT )
        {
            token = input.nextToken();
        }
        if( token != JsonToken.START_OBJECT )
        {
            String message = "Expected an object start at " + input.getCurrentLocation().toString();
            throw new ValueSerializationException( message );
        }
        return (ObjectNode) input.readValueAsTree();
    }

    @Override
    protected Object asSimpleValue( ModuleDescriptor module, JsonNode inputNode )
        throws Exception
    {
        if( isNullOrMissing( inputNode ) )
        {
            return null;
        }
        if( !inputNode.isValueNode() )
        {
            throw new ValueSerializationException( "Expected a value node but got a container node " + inputNode );
        }
        if( inputNode.isDouble() )
        {
            return inputNode.asDouble();
        }
        if( inputNode.isLong() )
        {
            return inputNode.asLong();
        }
        if( inputNode.isInt() )
        {
            return inputNode.asInt();
        }
        if( inputNode.isBoolean() )
        {
            return inputNode.asBoolean();
        }
        return inputNode.asText();
    }

    @Override
    @SuppressWarnings( "SimplifiableIfStatement" )
    protected boolean isObjectValue( ModuleDescriptor module, JsonNode inputNode )
        throws Exception
    {
        if( isNullOrMissing( inputNode ) )
        {
            return false;
        }
        return inputNode.isObject();
    }

    @Override
    protected boolean objectHasField( ModuleDescriptor module, JsonNode inputNode, String key )
        throws Exception
    {
        if( isNullOrMissing( inputNode ) )
        {
            return false;
        }
        if( !inputNode.isObject() )
        {
            throw new ValueSerializationException( "Expected an object but got " + inputNode );
        }
        return inputNode.has( key );
    }

    @Override
    protected <T> T getObjectFieldValue( ModuleDescriptor module,
                                         JsonNode inputNode,
                                         String key,
                                         Function<JsonNode, T> valueDeserializer
    )
        throws Exception
    {
        JsonNode valueNode = inputNode.get( key );
        if( isNullOrMissing( valueNode ) )
        {
            return null;
        }
        return valueDeserializer.apply( valueNode );
    }

    @Override
    protected <T> void putArrayNodeInCollection( ModuleDescriptor module,
                                                 JsonNode inputNode,
                                                 Function<JsonNode, T> deserializer,
                                                 Collection<T> collection
    )
        throws Exception
    {
        if( isNullOrMissing( inputNode ) )
        {
            return;
        }
        if( !inputNode.isArray() )
        {
            throw new ValueSerializationException( "Expected an array but got " + inputNode );
        }
        ArrayNode array = (ArrayNode) inputNode;
        for( JsonNode item : array )
        {
            T value = deserializer.apply( item );
            collection.add( value );
        }
    }

    @Override
    protected <V> void putObjectNodeInMap( ModuleDescriptor module,
                                           JsonNode inputNode,
                                           Function<JsonNode, V> valueDeserializer,
                                           Map<String, V> map
    )
        throws Exception
    {
        if( isNullOrMissing( inputNode ) )
        {
            return;
        }
        if( !inputNode.isObject() )
        {
            throw new ValueSerializationException( "Expected an object but got " + inputNode );
        }
        ObjectNode object = (ObjectNode) inputNode;
        Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
        while( fields.hasNext() )
        {
            Map.Entry<String, JsonNode> entry = fields.next();
            V value = valueDeserializer.apply( entry.getValue() );
            map.put( entry.getKey(), value );
        }
    }

    private static boolean isNullOrMissing( JsonNode inputNode )
    {
        return inputNode == null || inputNode.isNull() || inputNode.isMissingNode();
    }
}
