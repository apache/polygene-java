package org.qi4j.library.rdf.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.qi4j.api.composite.DependencyDescriptor;
import org.qi4j.api.composite.MethodDescriptor;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.mixin.MixinDescriptor;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.HierarchicalVisitor;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.qi4j.functional.Iterables.first;

/**
 * TODO
 */
public class Model2XML
    implements Function<ApplicationDescriptor, Document>
{
    private static Map<String, String> simpleMappings = new HashMap<String, String>();

    static
    {
        simpleMappings.put( "TransientsModel", "transients" );
        simpleMappings.put( "EntitiesModel", "entities" );
        simpleMappings.put( "ServicesModel", "services" );
        simpleMappings.put( "ImportedServicesModel", "importedservices" );
        simpleMappings.put( "ObjectsModel", "objects" );
        simpleMappings.put( "ValuesModel", "values" );
        simpleMappings.put( "MixinsModel", "mixins" );
        simpleMappings.put( "CompositeMethodsModel", "methods" );
        simpleMappings.put( "InjectedFieldsModel", "injectedfields" );
        simpleMappings.put( "InjectedFieldModel", "injectedfield" );
    }

    @Override
    public Document map( ApplicationDescriptor Application )
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = builder.newDocument();

            final Stack<Node> current = new Stack<Node>();
            current.push( document );

            Application.accept( new HierarchicalVisitor<Object, Object, DOMException>()
            {
                @Override
                public boolean visitEnter( Object visited )
                    throws DOMException
                {
                    String mapping = simpleMappings.get( visited.getClass().getSimpleName() );
                    if( mapping != null )
                    {
                        Node node = document.createElement( "mapping" );
                        current.push( node );
                    }
                    else if( visited instanceof ApplicationDescriptor )
                    {
                        ApplicationDescriptor applicationDescriptor = (ApplicationDescriptor) visited;
                        Node application = document.createElement( "application" );

                        addAttribute( "name", applicationDescriptor.name(), application );

                        current.push( application );
                    }
                    else if( visited instanceof LayerDescriptor )
                    {
                        LayerDescriptor layerDescriptor = (LayerDescriptor) visited;
                        Node layer = document.createElement( "layer" );

                        addAttribute( "name", layerDescriptor.name(), layer );

                        current.push( layer );
                    }
                    else if( visited instanceof ModuleDescriptor )
                    {
                        ModuleDescriptor moduleDescriptor = (ModuleDescriptor) visited;
                        Node module = document.createElement( "module" );

                        addAttribute( "name", moduleDescriptor.name(), module );

                        current.push( module );
                    }
                    else if( visited instanceof TransientDescriptor )
                    {
                        TransientDescriptor descriptor = (TransientDescriptor) visited;
                        Node node = document.createElement( "transient" );

                        addAttribute( "type", first(descriptor.types()).getName(), node );
                        addAttribute( "visibility", descriptor.visibility().name(), node );

                        current.push( node );
                    }
                    else if( visited instanceof MethodDescriptor )
                    {
                        MethodDescriptor descriptor = (MethodDescriptor) visited;
                        Node node = document.createElement( "method" );

                        addAttribute( "name", descriptor.method().getName(), node );

                        current.push( node );
                    }
                    else if( visited instanceof MixinDescriptor )
                    {
                        MixinDescriptor descriptor = (MixinDescriptor) visited;
                        Node node = document.createElement( "mixin" );

                        addAttribute( "class", descriptor.mixinClass().getName(), node );

                        current.push( node );
                    }
                    else if( visited instanceof DependencyDescriptor )
                    {
                        DependencyDescriptor descriptor = (DependencyDescriptor) visited;
                        Node node = document.createElement( "dependency" );

                        addAttribute( "annotation", descriptor.injectionAnnotation().toString(), node );
                        addAttribute( "injection", descriptor.injectionType().toString(), node );
                        addAttribute( "optional", Boolean.toString( descriptor.optional() ), node );

                        current.push( node );
                    }
                    else
                    {
                        Element element = document.createElement( visited.getClass().getSimpleName() );
                        current.push( element );
                    }

                    return true;
                }

                @Override
                public boolean visitLeave( Object visited )
                    throws DOMException
                {
                    Node node = current.pop();

                    if( node.getChildNodes().getLength() == 0 && node.getAttributes().getLength() == 0 )
                    {
                        return true;
                    }

                    current.peek().appendChild( node );
                    return true;
                }

                @Override
                public boolean visit( Object visited )
                    throws DOMException
                {
                    Element element;
                    if( visited instanceof DependencyDescriptor )
                    {
                        DependencyDescriptor descriptor = (DependencyDescriptor) visited;
                        element = document.createElement( "dependency" );

                        addAttribute( "annotation", descriptor.injectionAnnotation().toString(), element );
                        addAttribute( "injection", descriptor.injectionType().toString(), element );
                        addAttribute( "optional", Boolean.toString( descriptor.optional() ), element );
                    }
                    else
                    {
                        element = document.createElement( visited.getClass().getSimpleName() );
                    }

                    current.peek().appendChild( element );

                    return true;
                }

                private void addAttribute( String name, String value, Node node )
                {
                    Attr attr = document.createAttribute( name );
                    attr.setValue( value );
                    ( (Element) node ).setAttributeNode( attr );
                }
            } );

            return document;
        }
        catch( Exception exception )
        {
            throw new IllegalArgumentException( exception );
        }
    }
}
