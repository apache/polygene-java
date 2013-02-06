/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2010, Niclas Hehdman. All Rights Reserved.
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
package org.qi4j.valueserialization.orgjson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueDeserializer;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.functional.Function;
import org.qi4j.spi.value.ValueDeserializerAdapter;

/**
 * ValueDeserializer reading Values from JSON documents using org.json.
 */
public class OrgJsonValueDeserializer
    extends ValueDeserializerAdapter<JSONTokener, Object>
{

    public OrgJsonValueDeserializer(
        @Structure Application application,
        @Structure Module module,
        @Service ServiceReference<ValueDeserializer> serviceRef )
    {
        super( application, module, serviceRef );
    }

    /* package */ OrgJsonValueDeserializer(
        Application application,
        Module module,
        Function<Application, Module> valuesModuleFinder )
    {
        super( application, module, valuesModuleFinder );
    }

    @Override
    protected JSONTokener adaptInput( InputStream input )
        throws Exception
    {
        return new JSONTokener( new InputStreamReader( input, "UTF-8" ) );
    }

    @Override
    protected Object readPlainValue( JSONTokener input )
        throws Exception
    {
        Object nextValue = input.nextValue();
        if( JSONObject.NULL.equals( nextValue ) )
        {
            return null;
        }
        else // Object or Array
        if( JSONObject.class.isAssignableFrom( nextValue.getClass() )
            || JSONArray.class.isAssignableFrom( nextValue.getClass() ) )
        {
            throw new ValueSerializationException( "Asked for a Value but found an Object or an Array." );
        }
        return nextValue;
    }

    @Override
    protected <T> Collection<T> readArrayInCollection( JSONTokener input,
                                                       Function<JSONTokener, T> deserializer,
                                                       Collection<T> collection )
        throws Exception
    {
        char c = input.nextClean();
        char q;
        if( c == 'n' ) // null?
        {
            /*
             * Handle unquoted text. This could be the values true, false, or
             * null, or it can be a number. An implementation (such as this one)
             * is allowed to also accept non-standard forms.
             *
             * Accumulate characters until we reach the end of the text or a
             * formatting character.
             */
            StringBuilder sb = new StringBuilder();
            sb.setLength( 0 );
            while( c >= ' ' && ",:]}/\\\"[{;=#".indexOf( c ) < 0 )
            {
                sb.append( c );
                c = input.next();
            }
            input.back();
            String s = sb.toString().trim();
            if( !"null".equals( s ) )
            {
                input.syntaxError( "Unknown value: '" + s + "'" );
            }
            return null;
        }
        else if( c == '[' )
        {
            q = ']';
        }
        else
        {
            throw input.syntaxError( "A JSONArray text must start with '['" );
        }
        if( input.nextClean() == ']' )
        {
            return collection;
        }
        input.back();
        for( ;; )
        {
            if( input.nextClean() == ',' )
            {
                input.back();
                collection.add( null );
            }
            else
            {
                input.back();
                collection.add( deserializer.map( input ) );
            }
            c = input.nextClean();
            switch( c )
            {
                case ';':
                case ',':
                    if( input.nextClean() == ']' )
                    {
                        return collection;
                    }
                    input.back();
                    break;
                case ']':
                case ')':
                    if( q != c )
                    {
                        throw input.syntaxError( "Expected a '" + Character.valueOf( q ) + "'" );
                    }
                    return collection;
                default:
                    throw input.syntaxError( "Expected a ',' or ']'" );
            }
        }
    }

    @Override
    protected <K, V> Map<K, V> readMapInMap( JSONTokener input,
                                             Function<JSONTokener, K> keyDeserializer,
                                             Function<JSONTokener, V> valueDeserializer,
                                             Map<K, V> map )
        throws Exception
    {
        char c = input.nextClean();
        char q;
        if( c == 'n' ) // null?
        {
            /*
             * Handle unquoted text. This could be the values true, false, or
             * null, or it can be a number. An implementation (such as this one)
             * is allowed to also accept non-standard forms.
             *
             * Accumulate characters until we reach the end of the text or a
             * formatting character.
             */
            StringBuilder sb = new StringBuilder();
            sb.setLength( 0 );
            while( c >= ' ' && ",:]}/\\\"[{;=#".indexOf( c ) < 0 )
            {
                sb.append( c );
                c = input.next();
            }
            input.back();
            String s = sb.toString().trim();
            if( !"null".equals( s ) )
            {
                input.syntaxError( "Unknown value: '" + s + "'" );
            }
            return null;
        }
        else if( c == '[' )
        {
            q = ']';
        }
        else
        {
            throw input.syntaxError( "A JSONArray text must start with '['" );
        }
        if( input.nextClean() == ']' )
        {
            return map;
        }
        input.back();

        for( ;; )
        {
            if( input.nextClean() == ',' )
            {
                input.back();
            }
            else
            {
                input.back();
                // Map entry!
                if( input.nextClean() != '{' )
                {
                    throw input.syntaxError( "A JSONObject text must begin with '{'" );
                }

                String objectKey;
                K key = null;
                V value = null;

                boolean breakIteration = false;
                while( !breakIteration )
                {
                    c = input.nextClean();
                    switch( c )
                    {
                        case 0:
                            throw input.syntaxError( "A JSONObject text must end with '}'" );
                        case '}':
                            breakIteration = true;
                            continue;
                        default:
                            input.back();
                            objectKey = input.nextValue().toString();
                    }

                    /*
                     * The key is followed by ':'. We will also tolerate '=' or '=>'.
                     */
                    c = input.nextClean();
                    if( c == '=' )
                    {
                        if( input.next() != '>' )
                        {
                            input.back();
                        }
                    }
                    else if( c != ':' )
                    {
                        throw input.syntaxError( "Expected a ':' after a key" );
                    }

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
                        input.nextValue();
                    }

                    /*
                     * Pairs are separated by ','. We will also tolerate ';'.
                     */
                    switch( input.nextClean() )
                    {
                        case ';':
                        case ',':
                            if( input.nextClean() == '}' )
                            {
                                breakIteration = true;
                                continue;
                            }
                            input.back();
                            continue;
                        case '}':
                            breakIteration = true;
                            continue;
                        default:
                            throw input.syntaxError( "Expected a ',' or '}'" );
                    }
                }
                if( key != null )
                {
                    map.put( key, value );
                }
            }
            c = input.nextClean();
            switch( c )
            {
                case ';':
                case ',':
                    if( input.nextClean() == ']' )
                    {
                        return map;
                    }
                    input.back();
                    break;
                case ']':
                case ')':
                    if( q != c )
                    {
                        throw input.syntaxError( "Expected a '" + Character.valueOf( q ) + "'" );
                    }
                    return map;
                default:
                    throw input.syntaxError( "Expected a ',' or ']'" );
            }
        }
    }

    //
    // Deserialization - Tree parsing
    //
    @Override
    protected JSONObject readObjectTree( JSONTokener input )
        throws Exception
    {
        Object objectTree = input.nextValue();
        if( JSONObject.NULL.equals( objectTree ) )
        {
            return null;
        }
        return (JSONObject) objectTree;
    }

    @Override
    protected Object asSimpleValue( Object inputNode )
        throws Exception
    {
        if( JSONObject.NULL.equals( inputNode ) )
        {
            return null;
        }
        if( inputNode instanceof JSONObject || inputNode instanceof JSONArray )
        {
            throw new ValueSerializationException( "Expected a simple value but got " + inputNode );
        }
        return inputNode;
    }

    @Override
    protected boolean isObjectValue( Object inputNode )
        throws Exception
    {
        if( JSONObject.NULL.equals( inputNode ) )
        {
            return false;
        }
        return inputNode instanceof JSONObject;
    }

    @Override
    protected boolean objectHasField( Object inputNode, String key )
        throws Exception
    {
        if( JSONObject.NULL.equals( inputNode ) )
        {
            return false;
        }
        if( !( inputNode instanceof JSONObject ) )
        {
            throw new ValueSerializationException( "Expected an object but got " + inputNode );
        }
        JSONObject json = (JSONObject) inputNode;
        return json.has( key );
    }

    @Override
    protected <T> T getObjectFieldValue( Object inputNode, String key, Function<Object, T> valueDeserializer )
        throws Exception
    {
        JSONObject json = (JSONObject) inputNode;
        Object valueNode = json.opt( key );
        if( JSONObject.NULL.equals( valueNode ) )
        {
            return null;
        }
        T value = valueDeserializer.map( valueNode );
        return value;
    }

    @Override
    protected <T> void putArrayNodeInCollection( Object inputNode, Function<Object, T> deserializer, Collection<T> collection )
        throws Exception
    {
        if( JSONObject.NULL.equals( inputNode ) )
        {
            return;
        }
        if( !( inputNode instanceof JSONArray ) )
        {
            throw new ValueSerializationException( "Expected an array but got " + inputNode );
        }
        JSONArray array = (JSONArray) inputNode;
        for( int idx = 0; idx < array.length(); idx++ )
        {
            Object item = array.get( idx );
            T value = deserializer.map( item );
            collection.add( value );
        }
    }

    @Override
    protected <K, V> void putArrayNodeInMap( Object inputNode, Function<Object, K> keyDeserializer, Function<Object, V> valueDeserializer, Map<K, V> map )
        throws Exception
    {
        if( JSONObject.NULL.equals( inputNode ) )
        {
            return;
        }
        if( !( inputNode instanceof JSONArray ) )
        {
            throw new ValueSerializationException( "Expected an array but got " + inputNode );
        }
        JSONArray array = (JSONArray) inputNode;
        for( int idx = 0; idx < array.length(); idx++ )
        {
            Object item = array.get( idx );
            if( !( item instanceof JSONObject ) )
            {
                throw new ValueSerializationException( "Expected an object but got " + inputNode );
            }
            JSONObject object = (JSONObject) item;
            Object keyNode = object.get( "key" );
            Object valueNode = object.get( "value" );
            K key = keyDeserializer.map( keyNode );
            V value = valueDeserializer.map( valueNode );
            if( key != null )
            {
                map.put( key, value );
            }
        }
    }
}
