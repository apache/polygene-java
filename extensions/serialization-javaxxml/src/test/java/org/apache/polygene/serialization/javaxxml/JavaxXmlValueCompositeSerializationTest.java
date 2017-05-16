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
package org.apache.polygene.serialization.javaxxml;

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.serialization.Serializer;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.serialization.javaxxml.assembly.JavaxXmlSerializationAssembler;
import org.apache.polygene.spi.serialization.XmlSerialization;
import org.apache.polygene.test.serialization.AbstractValueCompositeSerializationTest;
import org.junit.Test;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class JavaxXmlValueCompositeSerializationTest extends AbstractValueCompositeSerializationTest
{
    // START SNIPPET: assembly
    @Override
    public void assemble( ModuleAssembly module )
    {
        new JavaxXmlSerializationAssembler().assemble( module );
        // END SNIPPET: assembly
        super.assemble( module );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    // START SNIPPET: xml-serialization
    @Service
    XmlSerialization xmlSerialization;
    // END SNIPPET: xml-serialization

    @Test
    public void valueCompositeXmlEquality()
    {
        // START SNIPPET: xml-serialization
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Some valueInstance = buildSomeValue( moduleInstance, uow, "42" );

            // Serialize using injected service
            String serializedXml = xmlSerialization.serialize( valueInstance );
            System.out.println( serializedXml );

            // Deserialize using Module API
            Some valueFromSerializedState = moduleInstance.newValueFromSerializedState( Some.class, serializedXml );
            assertThat( "Deserialized Value equality", valueInstance, equalTo( valueFromSerializedState ) );
            // END SNIPPET: xml-serialization

            // value.toString()
            // Need to loosely compare because of HashMaps not retaining order
            String valueXmlWithoutTypeInfo = xmlSerialization.serialize( Serializer.Options.NO_TYPE_INFO, valueFromSerializedState );
            assertThat( "value.toString() XML equality",
                        valueFromSerializedState.toString(),
                        isSimilarTo( valueXmlWithoutTypeInfo )
                            .withNodeMatcher( new DefaultNodeMatcher( ElementSelectors.byNameAndAllAttributes ) ) );
            // START SNIPPET: xml-serialization
        }
        // END SNIPPET: xml-serialization
    }
}
