/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.swing.binding.example.dom;

import java.util.List;
import org.qi4j.api.composite.CompositeBuilder;
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.swing.binding.tree.Container;
import org.qi4j.library.swing.binding.tree.Child;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public final class NodeListingContainerConcern extends ConcernOf<Container>
    implements Container
{
    @This private Node meAsNode;
    @Structure private CompositeBuilderFactory factory;

    public List<Child> children()
    {
        List<Child> children = next.children();

        if( children.size() == 0 )
        {
            NodeList nodes = meAsNode.getChildNodes();
            for( int i = 0; i < nodes.getLength(); i++ )
            {
                Node node = nodes.item( i );
                if( node instanceof Element )
                {
                    CompositeBuilder<ElementComposite> builder = factory.newCompositeBuilder( ElementComposite.class );
                    builder.use( node );
                    children.add( builder.newInstance() );
                }
                else if( node instanceof Text && !node.getTextContent().trim().equals( "" ) )
                {
                    CompositeBuilder<TextComposite> builder = factory.newCompositeBuilder( TextComposite.class );
                    builder.use( node );
                    children.add( builder.newInstance() );
                }
            }
            NamedNodeMap attributes = meAsNode.getAttributes();
            if( attributes != null )
            {
                for( int j = 0; j < attributes.getLength(); j++ )
                {
                    Node attrNode = attributes.item( j );
                    CompositeBuilder<AttributeComposite> builder = factory.newCompositeBuilder( AttributeComposite.class );
                    builder.use( attrNode );
                    AttributeComposite composite = builder.newInstance();
                    children.add( composite );
                }
            }
        }

        return children;
    }
}
