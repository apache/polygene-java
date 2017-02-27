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

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.spi.serialization.JsonSerialization;
import org.apache.polygene.test.serialization.AbstractValueCompositeSerializationTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JavaxJsonValueCompositeSerializationTest extends AbstractValueCompositeSerializationTest
{
    // START SNIPPET: assembly
    @Override
    public void assemble( ModuleAssembly module )
    {
        new JavaxJsonSerializationAssembler().assemble( module );
        // END SNIPPET: assembly
        super.assemble( module );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    // START SNIPPET: json-serialization
    @Service
    JsonSerialization jsonSerialization;
    // END SNIPPET: json-serialization

    @Test
    public void valueCompositeJsonEquality()
    {
        // START SNIPPET: json-serialization
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Some some = buildSomeValue( moduleInstance, uow, "42" );

            // Serialize using injected service
            JsonValue jsonState = jsonSerialization.toJson( some );
            String stateString = jsonState.toString();
            System.out.println( jsonState.toString() );

            // Deserialize using Module API
            Some some2 = moduleInstance.newValueFromSerializedState( Some.class, stateString );

            assertThat( "Deserialized Value equality", some, equalTo( some2 ) );

            JsonObject jsonState2 = Json.createReader( new StringReader( some2.toString() ) ).readObject();

            assertThat( "value.toString() JSON equality", jsonState, equalTo( jsonState2 ) );
        }
        // END SNIPPET: json-serialization
    }
}
