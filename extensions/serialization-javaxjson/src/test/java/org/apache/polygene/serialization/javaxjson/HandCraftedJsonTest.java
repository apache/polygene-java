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

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.serialization.javaxjson.assembly.JavaxJsonSerializationAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class HandCraftedJsonTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        new JavaxJsonSerializationAssembler().assemble( module );
        module.values( SomeValue.class );
    }

    public interface SomeValue
    {
        Property<String> foo();
    }

    @Service
    private Deserializer deserializer;

    @Test
    public void canReadSingleLineJson()
    {
        String json = "  {  \"foo\"  :  \"bar\"  }  ";
        assertThat( deserializer.deserialize( module, SomeValue.class, json ).foo().get(),
                    equalTo( "bar" ) );
    }

    @Test
    public void canReadFormattedMultiLineJson()
    {
        String json = "  \n {  \n\t\"foo\"  :  \"bar\"  \n }\n  ";
        assertThat( deserializer.deserialize( module, SomeValue.class, json ).foo().get(),
                    equalTo( "bar" ) );
    }

    @Test
    public void canReadCommentedJson()
    {
        String json = "// One comment\n {  \n\t\"foo\"  :  \"bar\"  \n/* Two comments */ }\n  ";
        assertThat( deserializer.deserialize( module, SomeValue.class, json ).foo().get(),
                    equalTo( "bar" ) );
    }
}
