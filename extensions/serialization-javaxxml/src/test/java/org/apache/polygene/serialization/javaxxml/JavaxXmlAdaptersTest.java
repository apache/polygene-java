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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.serialization.javaxxml.assembly.JavaxXmlSerializationAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class JavaxXmlAdaptersTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        new JavaxXmlSerializationAssembler().assemble( module );
        module.services( JavaxXmlSerialization.class )
              .withTypes( JavaxXmlAdapters.class );
    }

    @Service
    private JavaxXmlAdapters adapters;

    @Test
    public void test() throws ParserConfigurationException
    {
        JavaxXmlAdapter<String> adapter = adapters.adapterFor( String.class );
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        String original = "Cou<cou>€ôÙÔ#‰¥Ô";
        Node node = adapter.serialize( doc, original, null );
        assertThat( node.getNodeValue(), equalTo( original ) );
        String result = adapter.deserialize( node, null );
        assertThat( result, equalTo( original ) );
    }
}
