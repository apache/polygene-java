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
package org.qi4j.library.swing.visualizer.detailPanel.internal.tree;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.ToStringUtils.objectToString;
import org.qi4j.library.swing.visualizer.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ModuleDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 */
final class Qi4jTreeCellRenderer extends DefaultTreeCellRenderer
{
    private static final Icon ICON_APPLICATION;
    private static final Icon ICON_LAYER;
    private static final Icon ICON_MODULE;

    static
    {
        Class<Qi4jTreeCellRenderer> clazz = Qi4jTreeCellRenderer.class;
        ICON_APPLICATION = new ImageIcon( clazz.getResource( "application.png" ) );
        ICON_LAYER = new ImageIcon( clazz.getResource( "layer.png" ) );
        ICON_MODULE = new ImageIcon( clazz.getResource( "module.png" ) );
    }

    @Override
    public final Component getTreeCellRendererComponent(
        JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus )
    {
        // Safe the previous icon
        Icon prevLeafIcon = getLeafIcon();
        Icon prevOpenIcon = getOpenIcon();
        Icon prevCloseIcon = getClosedIcon();

        // Update icon
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Object userObject = treeNode.getUserObject();
        updateIcon( userObject );

        String treeNodeLabel = objectToString( userObject );
        Component component =
            super.getTreeCellRendererComponent( tree, treeNodeLabel, sel, expanded, leaf, row, hasFocus );

        // Restore icon
        setLeafIcon( prevLeafIcon );
        setOpenIcon( prevOpenIcon );
        setClosedIcon( prevCloseIcon );

        return component;
    }

    private void updateIcon( Object aValue )
    {
        if( aValue instanceof ApplicationDetailDescriptor )
        {
            overrideAllIcons( ICON_APPLICATION );
        }
        else if( aValue instanceof LayerDetailDescriptor )
        {
            overrideAllIcons( ICON_LAYER );
        }
        else if( aValue instanceof ModuleDetailDescriptor )
        {
            overrideAllIcons( ICON_MODULE );
        }
    }

    private void overrideAllIcons( Icon anIcon )
    {
        setLeafIcon( anIcon );
        setOpenIcon( anIcon );
        setClosedIcon( anIcon );
    }
}
