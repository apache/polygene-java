/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.valueserialization.jackson;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueDeserializer;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.functional.Function;
import org.qi4j.spi.value.ValueDeserializerAdapter;

/**
 * ValueDeserializer reading Values from JSON documents using Jackson.
 */
public class JacksonValueDeserializer
    extends ValueDeserializerAdapter<JsonParser, JsonNode>
{

    private final JsonFactory jsonFactory = new MappingJsonFactory();

    public JacksonValueDeserializer( @Structure Application application,
                                     @Structure Module module,
                                     @Service ServiceReference<ValueDeserializer> serviceRef )
    {
        super( application, module, serviceRef );
    }

    @Override
    protected JsonParser adaptInput( InputStream input )
        throws Exception
    {
        return jsonFactory.createJsonParser( input );
    }

    @Override
    protected void onDeserializationEnd( ValueType valueType, JsonParser input )
        throws Exception
    {
        input.close();
    }

    @Override
    protected Object readPlainValue( JsonParser input )
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
    protected <T> Collection<T> readArrayInCollection( JsonParser input,
                                                       Function<JsonParser, T> deserializer,
                                                       Collection<T> collection )
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
            T element = deserializer.map( input );
            collection.add( element );
        }
        return collection;
    }

    @Override
    protected <K, V> Map<K, V> readMapInMap( JsonParser input,
                                             Function<JsonParser, K> keyDeserializer,
                                             Function<JsonParser, V> valueDeserializer,
                                             Map<K, V> map )
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
                    key = keyDeserializer.map( input );
                }
                else if( "value".equals( objectKey ) )
                {
                    value = valueDeserializer.map( input );
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
    protected ObjectNode readObjectTree( JsonParser input )
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
            throw new ValueSerializationException( "Expected an object start at "
                                                   + input.getCurrentLocation().toString() );
        }
        return (ObjectNode) input.readValueAsTree();
    }

    @Override
    protected Object asSimpleValue( JsonNode inputNode )
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
    protected boolean isObjectValue( JsonNode inputNode )
        throws Exception
    {
        if( isNullOrMissing( inputNode ) )
        {
            return false;
        }
        return inputNode.isObject();
    }

    @Override
    protected boolean objectHasField( JsonNode inputNode, String key )
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
    protected <T> T getObjectFieldValue( JsonNode inputNode, String key, Function<JsonNode, T> valueDeserializer )
        throws Exception
    {
        JsonNode valueNode = inputNode.get( key );
        if( isNullOrMissing( valueNode ) )
        {
            return null;
        }
        T value = valueDeserializer.map( valueNode );
        return value;
    }

    @Override
    protected <T> void putArrayNodeInCollection( JsonNode inputNode,
                                                 Function<JsonNode, T> deserializer,
                                                 Collection<T> collection )
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
            T value = deserializer.map( item );
            collection.add( value );
        }
    }

    @Override
    protected <K, V> void putArrayNodeInMap( JsonNode inputNode,
                                             Function<JsonNode, K> keyDeserializer,
                                             Function<JsonNode, V> valueDeserializer,
                                             Map<K, V> map )
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
            if( !item.isObject() )
            {
                throw new ValueSerializationException( "Expected an object but got " + inputNode );
            }
            JsonNode keyNode = item.get( "key" );
            JsonNode valueNode = item.get( "value" );
            K key = keyDeserializer.map( keyNode );
            V value = valueDeserializer.map( valueNode );
            if( key != null )
            {
                map.put( key, value );
            }
        }
    }

    private static boolean isNullOrMissing( JsonNode inputNode )
    {
        return inputNode == null || inputNode.isNull() || inputNode.isMissingNode();
    }
}
