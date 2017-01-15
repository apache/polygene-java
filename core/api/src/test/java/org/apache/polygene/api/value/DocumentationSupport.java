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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.type.CollectionType;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Snippets:
 * - default : default ValueSerialization
 * - service : assembled service ValueSerialization
 * - lookup  : ValueSerialization values module finder
 */
public class DocumentationSupport
    extends AbstractPolygeneTest
{

    // START SNIPPET: default
    // START SNIPPET: service
    public interface SomeValue // (1)
    {

        Property<String> foo();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( SomeValue.class ); // (2)
        // END SNIPPET: default
        new OrgJsonValueSerializationAssembler().assemble( module ); // (3)
        new DefaultUnitOfWorkAssembler().assemble( module );
        // START SNIPPET: default
    }
    // END SNIPPET: default
    // END SNIPPET: service

    @Test
    // START SNIPPET: default
    public void defaultValueSerialization()
    {
        SomeValue someValue = someNewValueInstance(); // (3)
        String json = someValue.toString(); // (4)
        SomeValue someNewValue = valueBuilderFactory.newValueFromSerializedState( SomeValue.class, json ); // (5)
        // END SNIPPET: default

        assertThat( json, equalTo( "{\"foo\":\"bar\"}" ) );
        assertThat( someNewValue, equalTo( someValue ) );

        // START SNIPPET: default
    }

    // END SNIPPET: default
    // START SNIPPET: service
    @Service
    private ValueSerializer valueSerializer; // (4)
    @Service
    private ValueDeserializer valueDeserializer; // (4)

    // END SNIPPET: service
    @Test
    // START SNIPPET: service
    public void assembledDefaultServiceSerialization()
    {
        SomeValue someValue = someNewValueInstance(); // (5)
        String json = valueSerializer.serialize( someValue ); // (6)
        SomeValue someNewValue = valueDeserializer.deserialize( module, SomeValue.class, json ); // (7)
        // END SNIPPET: service

        assertThat( json, equalTo( "{\"foo\":\"bar\"}" ) );
        assertThat( someNewValue, equalTo( someValue ) );

        // START SNIPPET: service
    }
    // END SNIPPET: service

    static enum AcmeValue
    {

        foo, bar
    }

    @Test
    // START SNIPPET: stream
    public void assembledServiceStreamingSerialization()
    {
        // END SNIPPET: stream

        List<AcmeValue> dataSource = Arrays.asList( AcmeValue.values() );
        ByteArrayOutputStream targetStream = new ByteArrayOutputStream();

        // START SNIPPET: stream
        // (1)
        Iterable<AcmeValue> data = dataSource; // Eg. Entities converted to Values
        OutputStream output = targetStream; // Eg. streaming JSON over HTTP

        // (2)
        valueSerializer.serialize( data, output );
        // END SNIPPET: stream

        byte[] serialized = targetStream.toByteArray();
        ByteArrayInputStream sourceStream = new ByteArrayInputStream( serialized );

        // START SNIPPET: stream
        // (3)
        InputStream input = sourceStream; // Eg. reading incoming JSON

        // (4)
        List<AcmeValue> values = valueDeserializer.deserialize( module, CollectionType.listOf( AcmeValue.class ), input );
        // END SNIPPET: stream

        assertThat( values, equalTo( dataSource ) );

        // START SNIPPET: stream
    }
    // END SNIPPET: stream

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
        Function<AcmeValue, String> serialize = valueSerializer.serialize();

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
        Function<String, AcmeValue> deserialize = valueDeserializer.deserialize( module, AcmeValue.class );

        // Deserialization of a collection of AcmeValue from a String.
        // One serialized AcmeValue per line.
        // (6)
        List<AcmeValue> values = lines.map( deserialize ).collect( toList() );
        // END SNIPPET: io

        assertThat( dataSource, equalTo( values ) );

        // START SNIPPET: io
    }
    // END SNIPPET: io

    @Test
    // TODO Move to SPI !
    // TODO Include in each ValueSerialization extensions documentation
    public void assembledWithValuesModuleSerialization()
        throws Exception
    {
        Application app = new Energy4Java().newApplication( applicationFactory -> {
            Assembler[][][] pancakes = new Assembler[][][]
                {
                    {
                        {
                            valuesModule -> {
                                valuesModule.layer().setName( "SINGLE-Layer" );
                                valuesModule.setName( "VALUES-Module" );

                                valuesModule.values( SomeValue.class );
                                new DefaultUnitOfWorkAssembler().assemble( valuesModule );
                            }
                        },
                        {
                            servicesModule -> {
                                servicesModule.setName( "SERVICES-Module" );
                                new OrgJsonValueSerializationAssembler().
                                    assemble( servicesModule );
                            }
                        }
                    }
                };
            return applicationFactory.newApplicationAssembly( pancakes );
        } );
        app.activate();
        try
        {
            Module valuesModule = app.findModule( "SINGLE-Layer", "VALUES-Module" );
            SomeValue someValue = someNewValueInstance();

            Module servicesModule = app.findModule( "SINGLE-Layer", "SERVICES-Module" );
            ValueSerialization valueSerialization = servicesModule.findService( ValueSerialization.class ).get();

            String json = valueSerialization.serialize( someValue );
            assertThat( json, equalTo( "{\"foo\":\"bar\"}" ) );

            SomeValue someNewValue = valueSerialization.deserialize( module, SomeValue.class, json );
            assertThat( someNewValue, equalTo( someValue ) );
        }
        finally
        {
            app.passivate();
        }
    }

    private SomeValue someNewValueInstance(  )
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        builder.prototype().foo().set( "bar" );
        return builder.newInstance();
    }
}
