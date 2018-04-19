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
 */
package org.apache.polygene.serialization.javaxjson;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.serialization.javaxjson.assembly.JavaxJsonSerializationAssembler;
import org.apache.polygene.spi.serialization.JsonSerialization;
import org.apache.polygene.test.serialization.AbstractCollectionSerializationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

public class JavaxJsonCollectionSerializationTest extends AbstractCollectionSerializationTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        new JavaxJsonSerializationAssembler().assemble( module );
        module.values( SomeValue.class );
        super.assemble( module );
    }

    public interface SomeValue
    {
        Property<String> foo();
    }

    @Service
    private JsonSerialization jsonSerialization;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Test
    public void serializeMapWithStringKeysAsJsonObject()
    {
        Map<String, String> map = new HashMap<>();
        map.put( "foo", "bar" );
        map.put( "baz", "bazar" );

        JsonValue json = jsonSerialization.toJson( map );
        assertThat( json.getValueType(), is( JsonValue.ValueType.OBJECT ) );

        JsonObject jsonObject = (JsonObject) json;
        assertThat( jsonObject.getString( "foo" ), equalTo( "bar" ) );
        assertThat( jsonObject.getString( "baz" ), equalTo( "bazar" ) );

        MapType mapType = MapType.of( ValueType.STRING, ValueType.STRING );
        Map<String, String> map2 = jsonSerialization.fromJson( module, mapType, json );
        assertThat( map2, equalTo( map ) );
    }

    @Test
    public void deserializeJsonArrayOfEntryObjectsAsMapWithStringKeys()
    {
        JsonObjectBuilder fooEntryBuilder = jsonFactories.builderFactory().createObjectBuilder();
        fooEntryBuilder.add( "key", "foo" );
        fooEntryBuilder.add( "value", "bar" );
        JsonObject fooEntry = fooEntryBuilder.build();

        JsonObjectBuilder bazEntryBuilder = jsonFactories.builderFactory().createObjectBuilder();
        bazEntryBuilder.add( "key", "baz" );
        bazEntryBuilder.add( "value", "bazar" );
        JsonObject bazEntry = bazEntryBuilder.build();

        JsonArrayBuilder arrayBuilder = jsonFactories.builderFactory().createArrayBuilder();
        arrayBuilder.add( fooEntry );
        arrayBuilder.add( bazEntry );
        JsonArray jsonArray = arrayBuilder.build();

        MapType mapType = MapType.of( ValueType.STRING, ValueType.STRING );
        Map<String, String> map = jsonSerialization.fromJson( module, mapType, jsonArray );

        assertThat( map.get( "foo" ), equalTo( "bar" ) );
        assertThat( map.get( "baz" ), equalTo( "bazar" ) );
    }

    @Test
    public void serializeMapWithComplexKeyToJsonArrayOfEntryObjects()
    {
        Map<SomeValue, String> map = new LinkedHashMap<>();
        map.put( newSomeValue( "foo" ), "bar" );
        map.put( newSomeValue( "baz" ), "bazar" );

        JsonValue json = jsonSerialization.toJson( map );
        assertThat( json.getValueType(), is( JsonValue.ValueType.ARRAY ) );

        JsonArray jsonArray = (JsonArray) json;
        JsonObject fooEntry = jsonArray.getJsonObject( 0 );
        JsonObject bazEntry = jsonArray.getJsonObject( 1 );
        assertThat( fooEntry.getJsonObject( "key" ).getString( "foo" ), equalTo( "foo" ) );
        assertThat( fooEntry.getString( "value" ), equalTo( "bar" ) );
        assertThat( bazEntry.getJsonObject( "key" ).getString( "foo" ), equalTo( "baz" ) );
        assertThat( bazEntry.getString( "value" ), equalTo( "bazar" ) );

        MapType mapType = MapType.of( ValueCompositeType.of( api.valueDescriptorFor( map.keySet().iterator().next() ) ),
                                      ValueType.STRING );
        Map<SomeValue, String> map2 = jsonSerialization.fromJson( module, mapType, json );
        assertThat( map2, equalTo( map ) );
    }

    @Test
    public void serializeMapWithMixedKeysAsJsonArrayOfEntryObjects()
    {
        Map<Object, String> map = new LinkedHashMap<>();
        map.put( "foo", "bar" );
        map.put( newSomeValue( "baz" ), "bazar" );

        JsonValue json = jsonSerialization.toJson( map );
        assertThat( json.getValueType(), is( JsonValue.ValueType.ARRAY ) );

        JsonArray jsonArray = (JsonArray) json;
        JsonObject fooEntry = jsonArray.getJsonObject( 0 );
        JsonObject bazEntry = jsonArray.getJsonObject( 1 );
        assertThat( fooEntry.getString( "key" ), equalTo( "foo" ) );
        assertThat( fooEntry.getString( "value" ), equalTo( "bar" ) );
        assertThat( bazEntry.getJsonObject( "key" ).getString( "foo" ), equalTo( "baz" ) );
        assertThat( bazEntry.getString( "value" ), equalTo( "bazar" ) );
    }

    @Test
    public void dontKnowHowToDeserializeMapWithMixedKeys()
    {
        JsonObjectBuilder fooKeyBuilder = jsonFactories.builderFactory().createObjectBuilder();
        fooKeyBuilder.add( "foo", "foo" );
        fooKeyBuilder.add( "_type", SomeValue.class.getName() );
        JsonObject fooKey = fooKeyBuilder.build();

        JsonObjectBuilder fooEntryBuilder = jsonFactories.builderFactory().createObjectBuilder();
        fooEntryBuilder.add( "key", fooKey );
        fooEntryBuilder.add( "value", "bar" );
        JsonObject fooEntry = fooEntryBuilder.build();

        JsonObjectBuilder bazEntryBuilder = jsonFactories.builderFactory().createObjectBuilder();
        bazEntryBuilder.add( "key", "baz" );
        bazEntryBuilder.add( "value", "bazar" );
        JsonObject bazEntry = bazEntryBuilder.build();

        JsonArrayBuilder arrayBuilder = jsonFactories.builderFactory().createArrayBuilder();
        arrayBuilder.add( fooEntry );
        arrayBuilder.add( bazEntry );
        JsonArray jsonArray = arrayBuilder.build();

        MapType mapType = MapType.of( ValueType.OBJECT, ValueType.STRING );
        try
        {
            jsonSerialization.fromJson( module, mapType, jsonArray );
            fail( "Should have failed deserialization" );
        }
        catch( SerializationException ex )
        {
            assertThat( ex.getMessage(),
                        equalTo( "Don't know how to deserialize java.lang.Object from \"baz\"" ) );
        }
    }

    private SomeValue newSomeValue( String foo )
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        builder.prototype().foo().set( foo );
        return builder.newInstance();
    }
}
