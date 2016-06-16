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
 *
 *
 */
package org.apache.zest.valueserialization.stax;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.value.ValueSerializationException;
import org.apache.zest.spi.value.ValueDeserializerAdapter;
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

    public StaxValueDeserializer()
    {
        // Input Factory setup
        inputFactory.setProperty( "javax.xml.stream.isValidating", Boolean.FALSE );
        inputFactory.setProperty( "javax.xml.stream.isNamespaceAware", Boolean.FALSE );
        inputFactory.setProperty( "javax.xml.stream.supportDTD", Boolean.FALSE );
        inputFactory.setProperty( "javax.xml.stream.isCoalescing", Boolean.TRUE );
        inputFactory.setProperty( "javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE );
    }

    @Override
    protected XMLEventReader adaptInput( ModuleDescriptor module, InputStream input )
        throws Exception
    {
        return inputFactory.createXMLEventReader( input, "UTF-8" );
    }

    @Override
    protected Object readPlainValue( ModuleDescriptor module, XMLEventReader input )
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
    protected <T> Collection<T> readArrayInCollection( ModuleDescriptor module,
                                                       XMLEventReader input,
                                                       Function<XMLEventReader, T> deserializer,
                                                       Collection<T> collection
    )
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
            T item = deserializer.apply( input );
            collection.add( item );
        }
        return collection;
    }

    @Override
    protected <K, V> Map<K, V> readMapInMap( ModuleDescriptor module,
                                             XMLEventReader input,
                                             Function<XMLEventReader, K> keyDeserializer,
                                             Function<XMLEventReader, V> valueDeserializer,
                                             Map<K, V> map
    )
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
            if( !currentTag.isStartElement() || !"object".equals( currentTag.asStartElement()
                                                                      .getName()
                                                                      .getLocalPart() ) )
            {
                throw new ValueSerializationException( "Expected an <object/> but got: " + nextTag );
            }
            currentTag = input.nextTag(); // <field>
            K key = null;
            V value = null;
            while( !currentTag.isEndElement() || !"object".equals( currentTag.asEndElement()
                                                                       .getName()
                                                                       .getLocalPart() ) )
            {
                input.nextTag(); // <name>
                String keyOrValue = input.nextEvent().asCharacters().getData();
                input.nextTag(); // </name>
                input.nextTag(); // <value>
                switch( keyOrValue )
                {
                case "key":
                    key = keyDeserializer.apply( input );
                    break;
                case "value":
                    value = valueDeserializer.apply( input );
                    break;
                default:
                    readObjectTree( module, input );
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
    protected Node readObjectTree( ModuleDescriptor module, XMLEventReader input )
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
    protected Object asSimpleValue( ModuleDescriptor module, Node inputNode )
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
    @SuppressWarnings( "SimplifiableIfStatement" )
    protected boolean isObjectValue( ModuleDescriptor module, Node inputNode )
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
    protected boolean objectHasField( ModuleDescriptor module, Node inputNode, String key )
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
    protected <T> T getObjectFieldValue( ModuleDescriptor module,
                                         Node inputNode,
                                         String key,
                                         Function<Node, T> valueDeserializer
    )
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
        if( valueElement == null )
        {
            return null;
        }
        Node valueNode = valueElement.getFirstChild();
        if( valueNode == null )
        {
            return (T) "";
        }
        if( valueNode.getNodeType() == Node.ELEMENT_NODE && "null".equals( valueNode.getLocalName() ) )
        {
            return null;
        }
        return valueDeserializer.apply( valueNode );
    }

    @Override
    protected <T> void putArrayNodeInCollection( ModuleDescriptor module,
                                                 Node inputNode,
                                                 Function<Node, T> deserializer,
                                                 Collection<T> collection
    )
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
            T value = deserializer.apply( arrayValue.getFirstChild() );
            collection.add( value );
        }
    }

    @Override
    protected <V> void putObjectNodeInMap( ModuleDescriptor module,
                                           Node inputNode,
                                           Function<Node, V> valueDeserializer,
                                           Map<String, V> map
    )
        throws Exception
    {
        if( inputNode == null )
        {
            return;
        }
        if( !"object".equals( inputNode.getLocalName() ) )
        {
            throw new ValueSerializationException( "Expected an <object/> but got " + inputNode );
        }
        NodeList fieldsNodes = inputNode.getChildNodes();
        for( int idx = 0; idx < fieldsNodes.getLength(); idx++ )
        {
            Node fieldNode = fieldsNodes.item( idx );
            Node node = getDirectChildNode( fieldNode, "name" );
            String key = node != null ? node.getTextContent() : null;
            if( key != null && key.length() > 0 )
            {
                V value = getObjectFieldValue( module, inputNode, key, valueDeserializer );
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
