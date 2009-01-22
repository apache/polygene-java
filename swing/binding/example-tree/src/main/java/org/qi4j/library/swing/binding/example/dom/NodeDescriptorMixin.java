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

import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.ImmutableFacade;
import org.qi4j.api.property.Property;
import org.qi4j.library.swing.binding.tree.Descriptor;
import org.w3c.dom.Node;

/**
 * TODO
 */
public final class NodeDescriptorMixin
    implements Descriptor
{
    @State private Property<String> displayName;

    private void init( @Uses Node node )
    {
        String name;
        if( node.getNodeType() == Node.DOCUMENT_NODE )
        {
            name = node.getNodeName();
        }
        else if( node.getNodeType() == Node.ATTRIBUTE_NODE )
        {
            name = node.getNodeName() + "=" + node.getNodeValue();
        }
        else if( node.getNodeType() == Node.TEXT_NODE )
        {
            name = node.getTextContent();
        }
        else
        {
            name = node.getNodeName();
        }

        displayName.set( name );
    }

    public Property<String> displayName()
    {
        return new ImmutableFacade<String>( displayName );
    }
}
