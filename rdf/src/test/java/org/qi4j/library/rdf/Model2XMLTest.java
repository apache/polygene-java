package org.qi4j.library.rdf;

import org.junit.Test;
import org.qi4j.bootstrap.*;
import org.qi4j.library.rdf.model.Model2XML;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * TODO
 */
public class Model2XMLTest
{
    @Test
    public void testModel2XML() throws AssemblyException, TransformerException
    {
        Energy4Java is = new Energy4Java(  );
        ApplicationModelSPI model = is.newApplicationModel( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
            {
                ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();

                assembly.setName( "Test application" );

                return assembly;
            }
        } );

        Document document = new Model2XML().map( model );

        TransformerFactory.newInstance().newTransformer().transform( new DOMSource(document), new StreamResult(System.out) );
    }
}
