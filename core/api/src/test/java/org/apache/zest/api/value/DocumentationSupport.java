/*
 * Copyright (c) 2013, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.api.value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.zest.io.Inputs;
import org.apache.zest.io.Outputs;
import org.apache.zest.io.Transforms;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Snippets:
 * - default : default ValueSerialization
 * - service : assembled service ValueSerialization
 * - lookup  : ValueSerialization values module finder
 */
public class DocumentationSupport
    extends AbstractZestTest
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
        StringWriter outputWriter = new StringWriter();

        // START SNIPPET: io
        // (1)
        Iterable<AcmeValue> queryResult = dataSource; // Eg. Entities converted to Values
        Writer writer = outputWriter; // Eg. to pipe data to another process or to a file

        // (2)
        Function<AcmeValue, String> serialize = valueSerializer.serialize();

        // (3)
        Inputs.iterable( queryResult ).transferTo( Transforms.map( serialize, Outputs.text( writer ) ) );
        // END SNIPPET: io

        String string = writer.toString();
        StringReader inputReader = new StringReader( string );

        // START SNIPPET: io
        // (4)
        Reader reader = inputReader;
        List<AcmeValue> values = new ArrayList<AcmeValue>();

        // (5)
        Function<String, AcmeValue> deserialize = valueDeserializer.deserialize( module, AcmeValue.class );

        // Deserialization of a collection of AcmeValue from a String.
        // One serialized AcmeValue per line.
        // (6)
        Inputs.text( reader ).transferTo( Transforms.map( deserialize, Outputs.collection( values ) ) );
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

                                Function<Application, Module> valuesModuleFinder = new Function<Application, Module>()
                                {
                                    @Override
                                    public Module apply( Application app1 )
                                    {
                                        return app1.findModule( "SINGLE-Layer", "VALUES-Module" );
                                    }
                                };
                                new OrgJsonValueSerializationAssembler().
                                    withValuesModuleFinder( valuesModuleFinder ).
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
