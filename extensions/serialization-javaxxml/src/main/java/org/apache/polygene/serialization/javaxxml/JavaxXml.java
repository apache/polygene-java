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

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * javax.xml utilities.
 */
public class JavaxXml
{
    /**
     * Find child elements.
     *
     * @param parent the parent node
     * @return a stream of elements
     */
    public static Stream<Element> childElements( Node parent )
    {
        return toStream( parent.getChildNodes() ).filter( JavaxXml::isElement )
                                                 .map( JavaxXml::castToElement );
    }

    /**
     * Find the first child element.
     *
     * @param parent the parent node
     * @return an optional element
     */
    public static Optional<Element> firstChildElement( Node parent )
    {
        return childElements( parent ).findFirst();
    }

    /**
     * Find child elements named {@literal tagName}.
     *
     * @param parent the parent node
     * @param tagName the tag name
     * @return a stream of elements named {@literal tagName}
     */
    public static Stream<Element> childElementsNamed( Node parent, String tagName )
    {
        return childElements( parent ).filter( element -> tagName.equals( element.getTagName() ) );
    }

    /**
     * Find the first child element named {@literal tagName}.
     *
     * @param parent the parent node
     * @param tagName the tag name
     * @return an optional element named {@literal tagName}
     */
    public static Optional<Element> firstChildElementNamed( Node parent, String tagName )
    {
        return childElementsNamed( parent, tagName ).findFirst();
    }

    /**
     * Find child nodes holding state.
     *
     * @param parent the parent node
     * @return a stream or child state nodes
     */
    public static Stream<Node> stateChildNodes( Node parent )
    {
        return toStream( parent.getChildNodes() ).filter( JavaxXml::isStateNode );
    }

    /**
     * Find the first child node holding state.
     *
     * @param parent the parent node
     * @return an optional child state node
     */
    public static Optional<Node> firstStateChildNode( Node parent )
    {
        return stateChildNodes( parent ).findFirst();
    }

    /**
     * Test if a node holds state.
     *
     * Types of nodes holding state:
     * <ul>
     * <li>{@link Node#ELEMENT_NODE}</li>
     * <li>{@link Node#CDATA_SECTION_NODE}</li>
     * <li>{@link Node#TEXT_NODE}</li>
     * </ul>
     *
     * @param node the node
     * @return {@literal true} if {@literal node} holds state
     */
    public static boolean isStateNode( Node node )
    {
        switch( node.getNodeType() )
        {
            case Node.ELEMENT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isElement( Node node )
    {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }

    private static Element castToElement( Node node )
    {
        return (Element) node;
    }

    private static Stream<Node> toStream( NodeList nodeList )
    {
        return StreamSupport.stream( new Spliterators.AbstractSpliterator<Node>( Long.MAX_VALUE, Spliterator.ORDERED )
        {
            private int nextIndex = 0;

            @Override
            public boolean tryAdvance( Consumer<? super Node> action )
            {
                if( nextIndex >= nodeList.getLength() )
                {
                    return false;
                }
                action.accept( nodeList.item( nextIndex ) );
                nextIndex++;
                return true;
            }
        }, false );
    }

    private JavaxXml() {}
}
