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

import java.time.LocalDate;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomJsonAdapterTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        new JavaxJsonSerializationAssembler()
            .withJsonSettings( new JavaxJsonSettings().withJsonAdapter( new CustomValueAdapter() )
                                                      .withJsonAdapter( new CustomStructureAdapter() ) )
            .assemble( module );
        module.values( SomeValue.class );
    }

    interface SomeValue
    {
        Property<CustomValue> customValue();

        Property<CustomStructure> customStructure();
    }

    static class CustomValue
    {
        String state;

        CustomValue( String state )
        {
            this.state = state;
        }
    }

    static class CustomStructure
    {
        String foo;
        LocalDate bar;

        CustomStructure( String foo, LocalDate bar )
        {
            this.foo = foo;
            this.bar = bar;
        }
    }

    static class CustomValueAdapter implements JavaxJsonAdapter<CustomValue>
    {
        @Override
        public Class<CustomValue> type() { return CustomValue.class; }

        @Override
        public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
        {
            return JavaxJson.toJsonString( type().cast( object ).state );
        }

        @Override
        public CustomValue deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
        {
            switch( json.getValueType() )
            {
                case STRING:
                    return new CustomValue( ( (JsonString) json ).getString() );
                default:
                    throw new SerializationException( "Don't know how to deserialize CustomValue from " + json );
            }
        }
    }

    static class CustomStructureAdapter implements JavaxJsonAdapter<CustomStructure>
    {
        @Override
        public Class<CustomStructure> type() { return CustomStructure.class; }

        @Override
        public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
        {
            CustomStructure customStructure = type().cast( object );
            return Json.createObjectBuilder()
                       .add( "foo", customStructure.foo )
                       .add( "bar", serializeFunction.apply( customStructure.bar ) )
                       .build();
        }

        @Override
        public CustomStructure deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
        {
            if( json.getValueType() != JsonValue.ValueType.OBJECT )
            {
                throw new SerializationException( "Don't know how to deserialize CustomStructure from " + json );
            }
            JsonObject jsonObject = (JsonObject) json;
            String foo = jsonObject.getString( "foo" );
            LocalDate bar = (LocalDate) deserializeFunction.apply( jsonObject.get( "bar" ), ValueType.of( LocalDate.class ) );
            return new CustomStructure( foo, bar );
        }
    }

    @Service
    private JavaxJsonSerialization serialization;

    @Test
    public void customJsonAdapterForPropertyValue()
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        builder.prototype().customValue().set( new CustomValue( "custom-value-state" ) );
        builder.prototype().customStructure().set( new CustomStructure( "foo", LocalDate.of( 2017, 1, 1 ) ) );
        SomeValue someValue = builder.newInstance();

        System.out.println( someValue.toString() );

        JsonValue serialized = serialization.toJson( someValue );
        assertThat( serialized.getValueType(), is( JsonValue.ValueType.OBJECT ) );

        JsonObject jsonObject = (JsonObject) serialized;
        assertThat( jsonObject.getString( "customValue" ), equalTo( "custom-value-state" ) );
        JsonObject structure = jsonObject.getJsonObject( "customStructure" );
        assertThat( structure.getString( "foo" ), equalTo( "foo" ) );
        assertThat( structure.getString( "bar" ), equalTo( "2017-01-01" ) );

        SomeValue deserialized = serialization.fromJson( module, ValueCompositeType.of( SomeValue.class ), serialized );

        assertThat( deserialized.customValue().get().state, equalTo( "custom-value-state" ) );
        assertThat( deserialized.customStructure().get().foo, equalTo( "foo" ) );
        assertThat( deserialized.customStructure().get().bar, equalTo( LocalDate.of( 2017, 1, 1 ) ) );
    }

    @Test
    public void customJsonAdapterForDirectObject()
    {
        CustomValue customValueObject = new CustomValue( "custom-value-state" );
        JsonValue serialized = serialization.toJson( customValueObject );
        assertThat( serialized.getValueType(), is( JsonValue.ValueType.STRING ) );
        JsonString jsonString = (JsonString) serialized;
        assertThat( jsonString.getString(), equalTo( "custom-value-state" ) );

        CustomStructure customStructureObject = new CustomStructure( "foo", LocalDate.of( 2017, 1, 1 ) );
        serialized = serialization.toJson( customStructureObject );
        assertThat( serialized.getValueType(), is( JsonValue.ValueType.OBJECT ) );
        JsonObject jsonObject = (JsonObject) serialized;
        assertThat( jsonObject.getString( "foo" ), equalTo( "foo" ) );
        assertThat( jsonObject.getString( "bar" ), equalTo( "2017-01-01" ) );
    }
}
