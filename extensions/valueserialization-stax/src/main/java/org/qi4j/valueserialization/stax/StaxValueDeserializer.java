package org.qi4j.valueserialization.stax;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueDeserializer;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.functional.Function;
import org.qi4j.spi.value.ValueDeserializerAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ValueDeserializer reading Values from XML documents.
 */
public class StaxValueDeserializer
    extends ValueDeserializerAdapter<XMLEventReader, Node>
{

    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public StaxValueDeserializer( @Structure Application application,
                                  @Structure Module module,
                                  @Service ServiceReference<ValueDeserializer> serviceRef )
    {
        super( application, module, serviceRef );
        // Input Factory setup
        inputFactory.setProperty( "javax.xml.stream.isValidating", Boolean.FALSE );
        inputFactory.setProperty( "javax.xml.stream.isNamespaceAware", Boolean.FALSE );
        inputFactory.setProperty( "javax.xml.stream.supportDTD", Boolean.FALSE );
        inputFactory.setProperty( "javax.xml.stream.isCoalescing", Boolean.TRUE );
        inputFactory.setProperty( "javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE );
    }

    @Override
    protected XMLEventReader adaptInput( InputStream input )
        throws Exception
    {
        return inputFactory.createXMLEventReader( input, "UTF-8" );
    }

    @Override
    protected Object readPlainValue( XMLEventReader input )
        throws Exception
    {
        if( !input.hasNext() )
        {
            return null;
        }
        XMLEvent nextEvent = input.nextEvent();
        if( nextEvent.getEventType() == XMLEvent.START_ELEMENT
            && "null".equals( nextEvent.asStartElement().getName().getLocalPart() ) )
        {
            input.nextTag();
            return null;
        }
        if( nextEvent.getEventType() != XMLEvent.CHARACTERS )
        {
            throw new ValueSerializationException( "Expected characters but got: " + nextEvent );
        }
        String stringValue = nextEvent.asCharacters().getData();
        return detectAndConvertStringValue( stringValue );
    }

    @Override
    protected <T> Collection<T> readArrayInCollection( XMLEventReader input,
                                                       Function<XMLEventReader, T> deserializer,
                                                       Collection<T> collection )
        throws Exception
    {
        if( !input.hasNext() )
        {
            return null;
        }
        XMLEvent nextTag = input.nextTag();
        if( nextTag.isStartElement() && "null".equals( nextTag.asStartElement().getName().getLocalPart() ) )
        {
            input.nextTag();
            return null;
        }
        if( !nextTag.isStartElement() || !"array".equals( nextTag.asStartElement().getName().getLocalPart() ) )
        {
            throw new ValueSerializationException( "Expected an <array/> but got: " + nextTag );
        }
        WHILE:
        while( input.hasNext() )
        {
            XMLEvent currentTag = input.nextTag();
            if( currentTag.isEndElement() )
            {
                String endElementName = currentTag.asEndElement().getName().getLocalPart();
                switch( endElementName )
                {
                    case "array":
                        break WHILE;
                    case "value":
                        continue;
                }
            }
            if( !"value".equals( currentTag.asStartElement().getName().getLocalPart() ) )
            {
                throw new ValueSerializationException( "Expected a <value/> but got: " + currentTag );
            }
            T item = deserializer.map( input );
            collection.add( item );
        }
        return collection;
    }

    @Override
    protected <K, V> Map<K, V> readMapInMap( XMLEventReader input,
                                             Function<XMLEventReader, K> keyDeserializer,
                                             Function<XMLEventReader, V> valueDeserializer,
                                             Map<K, V> map )
        throws Exception
    {
        if( !input.hasNext() )
        {
            return null;
        }
        XMLEvent nextTag = input.nextTag();
        if( nextTag.isStartElement() && "null".equals( nextTag.asStartElement().getName().getLocalPart() ) )
        {
            input.nextTag();
            return null;
        }
        if( !nextTag.isStartElement() || !"array".equals( nextTag.asStartElement().getName().getLocalPart() ) )
        {
            throw new ValueSerializationException( "Expected an <array/> but got: " + nextTag );
        }
        XMLEvent currentTag = input.nextTag(); // <object>
        while( !currentTag.isEndElement() || !"array".equals( currentTag.asEndElement().getName().getLocalPart() ) )
        {
            if( !currentTag.isStartElement() || !"object".equals( currentTag.asStartElement().getName().getLocalPart() ) )
            {
                throw new ValueSerializationException( "Expected an <object/> but got: " + nextTag );
            }
            currentTag = input.nextTag(); // <field>
            K key = null;
            V value = null;
            while( !currentTag.isEndElement() || !"object".equals( currentTag.asEndElement().getName().getLocalPart() ) )
            {
                input.nextTag(); // <name>
                String keyOrValue = input.nextEvent().asCharacters().getData();
                input.nextTag(); // </name>
                input.nextTag(); // <value>
                switch( keyOrValue )
                {
                    case "key":
                        key = keyDeserializer.map( input );
                        break;
                    case "value":
                        value = valueDeserializer.map( input );
                        break;
                    default:
                        readObjectTree( input );
                        break;
                }
                input.nextTag(); // </value>
                input.nextTag(); // </field>
                currentTag = input.nextTag();
            }
            if( key != null )
            {
                map.put( key, value );
            }
            currentTag = input.nextTag();
        }
        return map;
    }

    @Override
    protected Node readObjectTree( XMLEventReader input )
        throws Exception
    {
        XMLEvent peek = input.peek();
        if( peek.isStartElement() && "null".equals( peek.asStartElement().getName().getLocalPart() ) )
        {
            input.nextTag();// <null>
            input.nextTag();// </null>
            return null;
        }
        String elementBody = readElementBody( input );
        Transformer transformer = transformerFactory.newTransformer();
        DOMResult domResult = new DOMResult();
        transformer.transform( new StreamSource( new StringReader( elementBody ) ), domResult );
        return ( (Document) domResult.getNode() ).getDocumentElement();

    }

    private static String readElementBody( XMLEventReader input )
        throws XMLStreamException
    {
        StringWriter buf = new StringWriter( 1024 );
        int depth = 0;
        while( input.hasNext() )
        {
            // peek event
            XMLEvent xmlEvent = input.peek();
            if( xmlEvent.isStartElement() )
            {
                ++depth;
            }
            else if( xmlEvent.isEndElement() )
            {
                --depth;
                // reached END_ELEMENT tag?
                // break loop, leave event in stream
                if( depth < 0 )
                {
                    break;
                }
            }
            // consume event
            xmlEvent = input.nextEvent();
            // print out event
            xmlEvent.writeAsEncodedUnicode( buf );
        }
        return buf.getBuffer().toString();
    }

    @Override
    protected Object asSimpleValue( Node inputNode )
        throws Exception
    {
        if( inputNode == null )
        {
            return null;
        }
        if( inputNode.getNodeType() == Node.ELEMENT_NODE && "null".equals( inputNode.getLocalName() ) )
        {
            return null;
        }
        if( inputNode.getNodeType() != Node.TEXT_NODE && inputNode.getNodeType() != Node.CDATA_SECTION_NODE )
        {
            throw new ValueSerializationException( "Expected a TEXT or CDATA node but got " + inputNode );
        }
        String stringValue = inputNode.getNodeValue();
        return detectAndConvertStringValue( stringValue );
    }

    @Override
    protected boolean isObjectValue( Node inputNode )
        throws Exception
    {
        if( inputNode == null )
        {
            return false;
        }
        if( "object".equals( inputNode.getLocalName() ) )
        {
            return true;
        }
        if( !"value".equals( inputNode.getLocalName() ) )
        {
            return false;
        }
        return getDirectChildNode( inputNode, "object" ) != null;
    }

    @Override
    protected boolean objectHasField( Node inputNode, String key )
        throws Exception
    {
        if( inputNode == null )
        {
            return false;
        }
        Node objectNode;
        if( "value".equals( inputNode.getLocalName() ) )
        {
            objectNode = getDirectChildNode( inputNode, "object" );
        }
        else
        {
            objectNode = inputNode;
        }
        if( objectNode == null )
        {
            return false;
        }
        if( !"object".equals( objectNode.getLocalName() ) )
        {
            throw new ValueSerializationException( "Expected an object value but got: " + objectNode );
        }
        return getObjectFieldNode( objectNode, key ) != null;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    protected <T> T getObjectFieldValue( Node inputNode,
                                         String key,
                                         Function<Node, T> valueDeserializer )
        throws Exception
    {
        if( inputNode == null )
        {
            return null;
        }
        Node objectNode;
        if( "value".equals( inputNode.getLocalName() ) )
        {
            objectNode = getDirectChildNode( inputNode, "object" );
        }
        else
        {
            objectNode = inputNode;
        }
        if( objectNode == null )
        {
            return null;
        }
        if( !"object".equals( objectNode.getLocalName() ) )
        {
            throw new ValueSerializationException( "Expected an object value but got: " + objectNode );
        }
        Node fieldNode = getObjectFieldNode( objectNode, key );
        if( fieldNode == null )
        {
            return null;
        }
        Node valueElement = getDirectChildNode( fieldNode, "value" );
        Node valueNode = valueElement.getFirstChild();
        if( valueNode == null )
        {
            return (T) "";
        }
        if( valueNode.getNodeType() == Node.ELEMENT_NODE && "null".equals( valueNode.getLocalName() ) )
        {
            return null;
        }
        T value = valueDeserializer.map( valueNode );
        return value;
    }

    @Override
    protected <T> void putArrayNodeInCollection( Node inputNode,
                                                 Function<Node, T> deserializer,
                                                 Collection<T> collection )
        throws Exception
    {
        if( inputNode == null )
        {
            return;
        }
        if( !( inputNode instanceof Element ) )
        {
            throw new ValueSerializationException( "Expected an Element but got " + inputNode );
        }
        NodeList arrayValues = inputNode.getChildNodes();
        for( int arrayValuesIndex = 0; arrayValuesIndex < arrayValues.getLength(); arrayValuesIndex++ )
        {
            Node arrayValue = arrayValues.item( arrayValuesIndex );
            T value = deserializer.map( arrayValue.getFirstChild() );
            collection.add( value );
        }
    }

    @Override
    protected <K, V> void putArrayNodeInMap( Node inputNode,
                                             Function<Node, K> keyDeserializer,
                                             Function<Node, V> valueDeserializer, Map<K, V> map )
        throws Exception
    {
        if( inputNode == null )
        {
            return;
        }
        if( !"array".equals( inputNode.getLocalName() ) )
        {
            throw new ValueSerializationException( "Expected an <array/> but got " + inputNode );
        }
        NodeList entriesNodes = inputNode.getChildNodes();
        for( int idx = 0; idx < entriesNodes.getLength(); idx++ )
        {
            Node entryNode = entriesNodes.item( idx );
            K key = getObjectFieldValue( entryNode, "key", keyDeserializer );
            V value = getObjectFieldValue( entryNode, "value", valueDeserializer );
            if( key != null )
            {
                map.put( key, value );
            }
        }
    }

    @SuppressWarnings( "AssignmentToMethodParameter" )
    private Object detectAndConvertStringValue( String stringValue )
    {
        if( stringValue == null || stringValue.length() == 0 )
        {
            return "";
        }
        stringValue = StringEscapeUtils.unescapeXml( stringValue );
        if( stringValue.matches( "[+-]?\\d+(\\.\\d+)?([eE][+-]?\\d+(\\.\\d+)?)?" ) )
        {
            if( stringValue.indexOf( '.' ) != -1 )
            {
                return new BigDecimal( stringValue );
            }
            return new BigInteger( stringValue );
        }
        if( "true".equalsIgnoreCase( stringValue ) || "false".equalsIgnoreCase( stringValue ) )
        {
            return Boolean.parseBoolean( stringValue );
        }
        return stringValue;
    }

    private static Node getObjectFieldNode( Node inputNode, String key )
    {
        if( inputNode == null )
        {
            return null;
        }
        if( !( inputNode instanceof Element ) )
        {
            throw new ValueSerializationException( "Excpected an Element but got " + inputNode );
        }
        NodeList fieldNodes = inputNode.getChildNodes();
        for( int idx = 0; idx < fieldNodes.getLength(); idx++ )
        {
            Node fieldNode = fieldNodes.item( idx );
            Node nameNode = getDirectChildNode( fieldNode, "name" );
            if( nameNode != null && key.equals( nameNode.getTextContent() ) )
            {
                return fieldNode;
            }
        }
        return null;
    }

    private static Node getDirectChildNode( Node parent, String name )
    {
        for( Node child = parent.getFirstChild(); child != null; child = child.getNextSibling() )
        {
            if( name.equals( child.getNodeName() ) )
            {
                return child;
            }
        }
        return null;
    }
}
