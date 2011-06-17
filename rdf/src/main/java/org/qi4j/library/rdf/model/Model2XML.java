package org.qi4j.library.rdf.model;

import org.qi4j.api.util.Function;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Stack;

/**
 * TODO
 */
public class Model2XML
    implements Function<ApplicationModelSPI, Document>
{
    @Override
    public Document map( ApplicationModelSPI applicationSPI )
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = builder.newDocument();

            applicationSPI.accept( new HierarchicalVisitor<Object, Object, DOMException>()
            {
                Stack<Node> current = new Stack<Node>();

                @Override
                public boolean visitEnter( Object visited ) throws DOMException
                {
                    if (visited instanceof ApplicationDescriptor)
                    {
                        ApplicationDescriptor applicationDescriptor = (ApplicationDescriptor) visited;
                        Node application = document.createElement( "application" );

                        addAttribute( "name", applicationDescriptor.name(), application );
                        
                        current.peek().appendChild( application );
                        current.push( application );
                    } else if (visited instanceof LayerDescriptor)
                    {
                        LayerDescriptor layerDescriptor = (LayerDescriptor) visited;
                        Node layer = document.createElement( "layer" );

                        addAttribute( "name", layerDescriptor.name(), layer );

                        current.peek().appendChild( layer );
                        current.push( layer );
                    } else if (visited instanceof ModuleDescriptor)
                    {
                        ModuleDescriptor moduleDescriptor = (ModuleDescriptor) visited;
                        Node module = document.createElement( "layer" );

                        addAttribute( "name", moduleDescriptor.name(), module );

                        current.peek().appendChild( module );
                        current.push( module );
                    } else
                    {
                        current.push( document.createElement( "" ) );
                        return false;
                    }

                    return true;
                }

                @Override
                public boolean visitLeave( Object visited ) throws DOMException
                {
                    current.pop();
                    return true;
                }

                @Override
                public boolean visit( Object visited ) throws DOMException
                {
                    return true;
                }

                private void addAttribute( String name, String value, Node node )
                {
                    Attr attr = document.createAttribute( "name" );
                    attr.setValue( value );
                    node.appendChild( attr );
                }
            } );

            return document;
        } catch (Exception exception)
        {
            throw new IllegalArgumentException( exception );
        }
    }
}
