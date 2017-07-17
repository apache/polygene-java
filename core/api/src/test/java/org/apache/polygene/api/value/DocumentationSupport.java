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
package org.apache.polygene.api.value;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.serialization.Serializer;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.serialization.javaxjson.assembly.JavaxJsonSerializationAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Snippets:
 * - default : default Serialization
 * - service : assembled service Serialization
 * - io : i/o usage
 */
public class DocumentationSupport extends AbstractPolygeneTest
{
    // START SNIPPET: default
    // START SNIPPET: service
    public interface SomeValue // (1)
    {
        Property<String> foo();
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        module.values( SomeValue.class ); // (2)
        // END SNIPPET: default
        // END SNIPPET: service
        // START SNIPPET: default
        module.defaultServices(); // (3)
        // END SNIPPET: default
        // START SNIPPET: service
        new JavaxJsonSerializationAssembler().assemble( module ); // (3)
        // END SNIPPET: service
        // START SNIPPET: default
        // START SNIPPET: service
    }
    // END SNIPPET: default
    // END SNIPPET: service

    @Test
    // START SNIPPET: default
    public void defaultSerialization()
    {
        SomeValue someValue = someNewValueInstance(); // (4)
        String json = someValue.toString(); // (5)
        SomeValue someNewValue = valueBuilderFactory.newValueFromSerializedState( SomeValue.class, json ); // (6)
        // END SNIPPET: default

        assertThat( json, equalTo( "{\"foo\":\"bar\"}" ) );
        assertThat( someNewValue, equalTo( someValue ) );

        // START SNIPPET: default
    }

    // END SNIPPET: default
    // START SNIPPET: service
    @Service
    private Serializer serializer; // (4)
    @Service
    private Deserializer deserializer; // (4)

    // END SNIPPET: service
    @Test
    // START SNIPPET: service
    public void assembledDefaultServiceSerialization()
    {
        SomeValue someValue = someNewValueInstance(); // (5)
        String json = serializer.serialize( someValue ); // (6)
        SomeValue someNewValue = deserializer.deserialize( module, SomeValue.class, json ); // (7)
        // END SNIPPET: service

        assertThat( json, equalTo( "{\"foo\":\"bar\"}" ) );
        assertThat( someNewValue, equalTo( someValue ) );

        // START SNIPPET: service
    }
    // END SNIPPET: service

    enum AcmeValue
    {
        foo,
        bar
    }

    @Test
    // START SNIPPET: io
    public void assembledServiceIOSerialization()
        throws IOException
    {
        // END SNIPPET: io

        List<AcmeValue> dataSource = Arrays.asList( AcmeValue.values() );
        StringWriter stringOutput = new StringWriter();
        PrintWriter output = new PrintWriter( stringOutput );


        // START SNIPPET: io
        // (1)
        // Eg. Entities converted to Values
        Stream<AcmeValue> queryResult = dataSource.stream();

        // (2)
        Function<AcmeValue, String> serialize = serializer.serializeFunction();

        // (3)
        // Eg. pipe data to another process or to a file
        queryResult.map( serialize ).forEach( output::println );
        // END SNIPPET: io

        output.flush();
        String string = stringOutput.toString();
        List<String> input = Arrays.asList( string.split( System.lineSeparator() ) );

        // START SNIPPET: io
        // (4)
        Stream<String> lines = input.stream();

        // (5)
        Function<String, AcmeValue> deserialize = deserializer.deserializeFunction( module, AcmeValue.class );

        // Deserialization of a collection of AcmeValue from a String.
        // One serialized AcmeValue per line.
        // (6)
        List<AcmeValue> values = lines.map( deserialize ).collect( toList() );
        // END SNIPPET: io

        assertThat( dataSource, equalTo( values ) );

        // START SNIPPET: io
    }
    // END SNIPPET: io

    private SomeValue someNewValueInstance()
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        builder.prototype().foo().set( "bar" );
        return builder.newInstance();
    }
}
