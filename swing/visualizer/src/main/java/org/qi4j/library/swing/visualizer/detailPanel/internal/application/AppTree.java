/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.swing.visualizer.detailPanel.internal.application;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.ToStringUtils.objectToString;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class AppTree extends JTree
{
    AppTree( TreeNode aRootNode, TreeSelectionListener aListener )
        throws IllegalArgumentException
    {
        super( aRootNode );

        validateNotNull( "aRootNode", aRootNode );
        validateNotNull( "aListener", aListener );

        // Selection settings
        TreeSelectionModel selectionModel = getSelectionModel();
        selectionModel.setSelectionMode( SINGLE_TREE_SELECTION );
        selectionModel.addTreeSelectionListener( aListener );

        // Cell renderer settings
        TreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer();
        setCellRenderer( treeCellRenderer );
    }

    @Override
    public final String convertValueToText(
        Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus )
    {
        if( value == null )
        {
            return "";
        }

        Class<?> valueClass = value.getClass();
        if( DefaultMutableTreeNode.class.isAssignableFrom( valueClass ) )
        {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            value = treeNode.getUserObject();
        }

        return objectToString( value );
    }
}
