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
