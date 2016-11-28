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
package org.apache.zest.bootstrap.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.json.JSONException;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.apache.zest.bootstrap.ClassScanner.findClasses;
import static org.apache.zest.bootstrap.ClassScanner.matches;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ApplicationBuilderTest
{
    @Test
    public void givenBuilderUseWhenBuildingApplicationExpectSuccess()
        throws AssemblyException, ActivationException
    {
        ApplicationBuilder builder = new ApplicationBuilder( "Build from API test." );
        builder.withLayer( "layer1" ).using( "layer2" ).using( "layer3" );
        builder.withLayer( "layer2" );
        builder.withLayer( "layer3" )
               .withModule( "test module" )
               .withAssemblers( findClasses( getClass() ).filter( matches( ".*ServiceAssembler" ) )
                                                         .collect( toList() ) );
        Application application = builder.newApplication();
        Module module = application.findModule( "layer3", "test module" );
        TestService service = module.findService( TestService.class ).get();
        assertThat( service.sayHello(), equalTo( "Hello Zest!" ) );
    }

    @Test
    public void givenJsonWhenBuildingApplicationExpectSuccess()
        throws JSONException, ActivationException, AssemblyException
    {
        ApplicationBuilder builder = ApplicationBuilder.fromJson( APPLICATION );
        Application application = builder.newApplication();
        Module module = application.findModule( "layer3", "test module" );
        TestService service = module.findService( TestService.class ).get();
        assertThat( service.sayHello(), equalTo( "Hello Zest!" ) );
    }

    @Test
    public void givenJsonInputStreamWhenBuildingApplicationExpectSuccess()
        throws IOException, JSONException, ActivationException, AssemblyException
    {
        InputStream input = new ByteArrayInputStream( APPLICATION.getBytes( "UTF-8" ) );
        ApplicationBuilder builder = ApplicationBuilder.fromJson( input );
        Application application = builder.newApplication();
        Module module = application.findModule( "layer3", "test module" );
        TestService service = module.findService( TestService.class ).get();
        assertThat( service.sayHello(), equalTo( "Hello Zest!" ) );
    }


    private static final String APPLICATION =
        "{\n" +
        "    \"name\": \"Build from JSON test.\",\n" +
        "    \"layers\": [\n" +
        "        {\n" +
        "            \"name\": \"layer1\",\n" +
        "            \"uses\": [ \"layer2\", \"layer3\"]\n" +
        "        },\n" +
        "        {\n" +
        "            \"name\": \"layer2\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"name\": \"layer3\",\n" +
        "            \"modules\" : [\n" +
        "                {\n" +
        "                    \"name\" : \"test module\",\n" +
        "                    \"assemblers\" : [\n" +
        "                            \"org.apache.zest.bootstrap.builder.ApplicationBuilderTest$TestServiceAssembler\"\n" +
        "                    ]\n" +
        "                }\n" +
        "            ]\n" +
        "        }\n" +
        "    ]\n" +
        "}";

    public static class TestServiceAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.addServices( TestService.class );
        }
    }

    @Mixins( TestService.TestMixin.class )
    public interface TestService
    {
        String sayHello();

        class TestMixin
            implements TestService
        {

            @Override
            public String sayHello()
            {
                return "Hello Zest!";
            }
        }
    }
}
