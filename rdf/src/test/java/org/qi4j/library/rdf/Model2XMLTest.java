package org.qi4j.library.rdf;

import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.*;
import org.qi4j.library.rdf.model.Model2XML;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
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
        ApplicationDescriptor model = is.newApplicationModel( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
            {
                ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();

                assembly.setName( "Test application" );

                LayerAssembly webLayer = assembly.layer( "Web" );
                LayerAssembly domainLayer = assembly.layer( "Domain" );
                LayerAssembly infrastructureLayer = assembly.layer( "Infrastructure" );

                webLayer.uses( domainLayer, infrastructureLayer );
                domainLayer.uses( infrastructureLayer );

                ModuleAssembly rest = webLayer.module( "REST" );
                rest.transients( TestTransient.class ).visibleIn( Visibility.layer );
                
                domainLayer.module( "Domain" );
                infrastructureLayer.module( "Database" );

                return assembly;
            }
        } );

        Document document = new Model2XML().map( model );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty( "indent", "yes"  );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2"  );
        transformer.transform( new DOMSource( document ), new StreamResult( System.out ) );
    }

    interface TestTransient
        extends TransientComposite
    {}
}
