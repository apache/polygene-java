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
package org.qi4j.api.value;

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
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.CollectionType;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Function;
import org.qi4j.io.Inputs;
import org.qi4j.io.Outputs;
import org.qi4j.io.Transforms;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Snippets:
 * - default : default ValueSerialization
 * - service : assembled service ValueSerialization
 * - lookup  : ValueSerialization values module finder
 */
public class DocumentationSupport
    extends AbstractQi4jTest
{

    @Before
    public void injectToThis()
    {
        module.injectTo( this );
    }

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
        // START SNIPPET: default
    }
    // END SNIPPET: default
    // END SNIPPET: service

    @Test
    // START SNIPPET: default
    public void defaultValueSerialization()
    {
        SomeValue someValue = someNewValueInstance( module ); // (3)
        String json = someValue.toString(); // (4)
        SomeValue someNewValue = module.newValueFromSerializedState( SomeValue.class, json ); // (5)
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
        SomeValue someValue = someNewValueInstance( module ); // (5)
        String json = valueSerializer.serialize( someValue ); // (6)
        SomeValue someNewValue = valueDeserializer.deserialize( SomeValue.class, json ); // (7)
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
        List<AcmeValue> values = valueDeserializer.deserialize( CollectionType.listOf( AcmeValue.class ), input );
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
        Function<String, AcmeValue> deserialize = valueDeserializer.deserialize( AcmeValue.class );

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
        Application app = new Energy4Java().newApplication( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                Assembler[][][] pancakes = new Assembler[][][]
                {
                    {
                        {
                            new Assembler()
                            {
                                @Override
                                public void assemble( ModuleAssembly valuesModule )
                                    throws AssemblyException
                                {
                                    valuesModule.layer().setName( "SINGLE-Layer" );
                                    valuesModule.setName( "VALUES-Module" );

                                    valuesModule.values( SomeValue.class );
                                }
                            }
                        },
                        {
                            new Assembler()
                            {
                                @Override
                                public void assemble( ModuleAssembly servicesModule )
                                    throws AssemblyException
                                {
                                    servicesModule.setName( "SERVICES-Module" );

                                    Function<Application, Module> valuesModuleFinder = new Function<Application, Module>()
                                    {
                                        @Override
                                        public Module map( Application app )
                                        {
                                            return app.findModule( "SINGLE-Layer", "VALUES-Module" );
                                        }
                                    };
                                    new OrgJsonValueSerializationAssembler().
                                        withValuesModuleFinder( valuesModuleFinder ).
                                        assemble( servicesModule );
                                }
                            }
                        }
                    }
                };
                return applicationFactory.newApplicationAssembly( pancakes );
            }
        } );
        app.activate();
        try
        {
            Module valuesModule = app.findModule( "SINGLE-Layer", "VALUES-Module" );
            SomeValue someValue = someNewValueInstance( valuesModule );

            Module servicesModule = app.findModule( "SINGLE-Layer", "SERVICES-Module" );
            ValueSerialization valueSerialization = servicesModule.findService( ValueSerialization.class ).get();

            String json = valueSerialization.serialize( someValue );
            assertThat( json, equalTo( "{\"foo\":\"bar\"}" ) );

            SomeValue someNewValue = valueSerialization.deserialize( SomeValue.class, json );
            assertThat( someNewValue, equalTo( someValue ) );
        }
        finally
        {
            app.passivate();
        }
    }

    private SomeValue someNewValueInstance( Module module )
    {
        ValueBuilder<SomeValue> builder = module.newValueBuilder( SomeValue.class );
        builder.prototype().foo().set( "bar" );
        return builder.newInstance();
    }
}
