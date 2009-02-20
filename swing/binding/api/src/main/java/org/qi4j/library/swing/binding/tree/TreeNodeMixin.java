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
package org.qi4j.library.swing.binding.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.CompositeBuilder;
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;

/**
 * JAVADOC
 */
public final class TreeNodeMixin
    implements TreeNode
{
    @Structure private CompositeBuilderFactory factory;
    private Container meAsContainer;
    private Child meAsChild;

    private List<TreeNode> childNodes;

    public TreeNodeMixin( @Optional @Uses Container meAsContainer, @Optional @Uses Child meAsChild )
    {
        this.meAsContainer = meAsContainer;
        this.meAsChild = meAsChild;
    }

    public TreeNode getChildAt( int childIndex )
    {
        if( meAsContainer == null )
        {
            return null;
        }
        checkChildren();
        return childNodes.get( childIndex );
    }

    public int getChildCount()
    {
        if( meAsContainer == null )
        {
            return -1;
        }
        checkChildren();
        return childNodes.size();
    }

    public TreeNode getParent()
    {
        if( meAsChild == null )
        {
            return null;
        }
        CompositeBuilder<TreeNodeComposite> builder = factory.newCompositeBuilder( TreeNodeComposite.class );
        builder.use( meAsChild );
        return builder.newInstance();
    }

    public int getIndex( TreeNode node )
    {
        if( meAsContainer == null )
        {
            return -1;
        }
        checkChildren();
        return childNodes.indexOf( node );
    }

    public boolean getAllowsChildren()
    {
        return meAsContainer != null;
    }

    public boolean isLeaf()
    {
        return meAsContainer == null;
    }

    public Enumeration children()
    {
        if( meAsContainer == null )
        {
            return null;
        }
        checkChildren();
        return Collections.enumeration( childNodes );
    }

    void checkChildren()
    {
        if( childNodes == null )
        {
            childNodes = new ArrayList<TreeNode>();
            for( Child child : meAsContainer.children() )
            {
                CompositeBuilder<TreeNodeComposite> builder = factory.newCompositeBuilder( TreeNodeComposite.class );
//                Container parent = null;
//                if( child instanceof Container )
//                {
//                    parent = (Container) child;
//                }
//                TreeNodeMixin treeNode = new TreeNodeMixin( parent, child );
//                builder.setMixin( TreeNode.class, treeNode );
                builder.use( child );
                childNodes.add( builder.newInstance() );
            }
        }
    }
}