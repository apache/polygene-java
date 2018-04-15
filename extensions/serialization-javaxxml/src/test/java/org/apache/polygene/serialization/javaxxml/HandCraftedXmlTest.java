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

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.serialization.javaxxml.assembly.JavaxXmlSerializationAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class HandCraftedXmlTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        new JavaxXmlSerializationAssembler().assemble( module );
        module.values( SomeValue.class );
    }

    public interface SomeValue
    {
        @Optional
        Property<String> foo();
    }

    @Service
    private Deserializer deserializer;

    @Test
    public void canReadSingleLineXml()
    {
        String xml = "<state><value><foo>bar</foo></value></state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar" ) );
    }

    @Test
    public void canReadMultiLineXml()
    {
        String xml = "<state>\n<value>\n<foo>bar</foo>\n</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar" ) );
    }

    @Test
    public void canReadIndentedMultiLineXml()
    {
        String xml = "<state>\n\t<value>\n\t\t<foo>bar</foo>\n\t</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar" ) );
    }

    @Test
    public void canReadSingleLineXmlWithMultilineStringValue()
    {
        String xml = "<state><value><foo>bar\nbaz\n</foo></value></state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar\nbaz\n" ) );
    }

    @Test
    public void canReadMultiLineXmlWithMultilineStringValue()
    {
        String xml = "<state>\n<value>\n<foo>bar\nbaz\n</foo>\n</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar\nbaz\n" ) );
    }

    @Test
    public void canReadIndentedMultiLineXmlWithMultilineStringValue()
    {
        String xml = "<state>\n\t<value>\n\t\t<foo>bar\nbaz\n</foo>\n\t</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar\nbaz\n" ) );
    }

    @Test
    public void canReadCommentedXml()
    {
        String xml = "<state><value><!-- Some comment --><foo>bar</foo></value></state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar" ) );
    }

    @Test
    public void canReadMultilineCommentedXml()
    {
        String xml = "<state>\n<value>\n<!-- Some comment -->\n<foo>bar</foo>\n</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar" ) );
    }

    @Test
    public void canReadIndentedMultilineCommentedXml()
    {
        String xml = "<state>\n\t<value>\n\t\t<!-- Some comment -->\n\t\t<foo>bar</foo>\n\t</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar" ) );
    }

    @Test
    public void canReadIndentedMultilineCommentedXmlWithMultilineStringValue()
    {
        String xml = "<state>\n\t<value>\n\t\t<!-- Some comment -->\n\t\t<foo>bar\nbaz\n</foo>\n\t</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "bar\nbaz\n" ) );
    }

    @Test
    public void canReadIndentedMultilineCommentedXmlWithTextValueStartingWithNewLine()
    {
        String xml
            = "<state>\n\t<value>\n\t\t<!-- Some comment -->\n\t\t<foo>\n\t\t\tbar\n\t\t\tbaz\n</foo>\n\t</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(),
                    equalTo( "\n\t\t\tbar\n\t\t\tbaz\n" ) );
    }

    @Test
    public void canReadIndentedMultilineXmlWithNullValues()
    {
        String xml = "<state>\n\t<value>\n\t\t<foo>\n<null/>\n</foo>\n\t</value>\n</state>";
        assertThat( deserializer.deserialize( module, SomeValue.class, xml ).foo().get(), nullValue() );
    }
}
