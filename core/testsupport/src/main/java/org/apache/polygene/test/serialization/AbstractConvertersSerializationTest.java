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
package org.apache.polygene.test.serialization;

import java.util.Objects;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.ConvertedBy;
import org.apache.polygene.api.serialization.Converter;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class AbstractConvertersSerializationTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        module.values( SomeValue.class );
        module.forMixin( SomeValue.class )
              .setMetaInfo( new CustomPropertyConverter() )
              .declareDefaults()
              .customAssemblyConvertedProperty();
    }

    protected abstract String getStringFromValueState( String state, String key ) throws Exception;

    public interface SomeValue
    {
        Property<CustomPlainValue> customPlainValue();

        @ConvertedBy( CustomPropertyConverter.class )
        Property<String> customConvertedProperty();

        Property<String> customAssemblyConvertedProperty();
    }

    @ConvertedBy( CustomPlainValueConverter.class )
    public static class CustomPlainValue
    {
        private final String state;

        CustomPlainValue( String state )
        {
            this.state = state;
        }

        public String getState()
        {
            return state;
        }

        @Override
        public boolean equals( final Object o )
        {
            if( this == o ) { return true; }
            if( o == null || getClass() != o.getClass() ) { return false; }
            CustomPlainValue that = (CustomPlainValue) o;
            return Objects.equals( state, that.state );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( state );
        }
    }

    public static class CustomPlainValueConverter implements Converter<CustomPlainValue>
    {
        @Override
        public Class<CustomPlainValue> type()
        {
            return CustomPlainValue.class;
        }

        @Override
        public String toString( CustomPlainValue object )
        {
            return rot13( object.getState() );
        }

        @Override
        public CustomPlainValue fromString( String string )
        {
            return new CustomPlainValue( rot13( string ) );
        }
    }

    public static class CustomPropertyConverter implements Converter<String>
    {
        @Override
        public Class<String> type()
        {
            return String.class;
        }

        @Override
        public String toString( String object )
        {
            return rot13( object );
        }

        @Override
        public String fromString( String string )
        {
            return rot13( string );
        }
    }

    @Service
    private Serialization serialization;

    @Test
    public void testConvertedByAnnotation() throws Exception
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        builder.prototype().customPlainValue().set( new CustomPlainValue( "foo" ) );
        builder.prototype().customConvertedProperty().set( "bar" );
        builder.prototype().customAssemblyConvertedProperty().set( "bazar" );
        SomeValue value = builder.newInstance();

        String serialized = serialization.serialize( value );
        assertThat( getStringFromValueState( serialized, "customPlainValue" ),
                    equalTo( rot13( "foo" ) ) );
        assertThat( getStringFromValueState( serialized, "customConvertedProperty" ),
                    equalTo( rot13( "bar" ) ) );
        assertThat( getStringFromValueState( serialized, "customAssemblyConvertedProperty" ),
                    equalTo( rot13( "bazar" ) ) );

        SomeValue deserialized = serialization.deserialize( module, SomeValue.class, serialized );
        assertThat( deserialized, equalTo( value ) );
    }

    private static String rot13( String string )
    {
        StringBuilder builder = new StringBuilder();
        for( int i = 0; i < string.length(); i++ )
        {
            char c = string.charAt( i );
            if( c >= 'a' && c <= 'm' ) { c += 13; }
            else if( c >= 'A' && c <= 'M' ) { c += 13; }
            else if( c >= 'n' && c <= 'z' ) { c -= 13; }
            else if( c >= 'N' && c <= 'Z' ) { c -= 13; }
            builder.append( c );
        }
        return builder.toString();
    }
}
