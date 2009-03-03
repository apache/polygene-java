/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.library.swing.envisage.tree;

import java.awt.Color;
import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ValueDetailDescriptor;
import org.qi4j.library.swing.envisage.util.ColorUtilities;

/**
 * TreeCellRenderer
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class TreeModelCellRenderer extends DefaultTreeCellRenderer
{
    protected ResourceBundle bundle = ResourceBundle.getBundle(this.getClass().getName());

    protected Icon applicationIcon;
    protected Icon layerIcon;
    protected Icon moduleIcon;
    protected Icon serviceIcon;
    protected Icon entityIcon;
    protected Icon valueIcon;
    protected Icon transientIcon;
    protected Icon objectIcon;

    protected Color defaultColor;
    protected Color serviceColor;
    protected Color entityColor;
    protected Color valueColor;
    protected Color transientColor;
    protected Color objectColor;

    public TreeModelCellRenderer()
    {
        try
        {
            applicationIcon = new ImageIcon(getClass().getResource(bundle.getString("ICON_Application")));
            layerIcon = new ImageIcon(getClass().getResource(bundle.getString("ICON_Layer")));
            moduleIcon = new ImageIcon(getClass().getResource(bundle.getString("ICON_Module")));
            serviceIcon = new ImageIcon(getClass().getResource(bundle.getString("ICON_Service")));
            entityIcon = new ImageIcon(getClass().getResource(bundle.getString("ICON_Entity")));
            valueIcon = new ImageIcon(getClass().getResource(bundle.getString("ICON_Value")));
            transientIcon = new ImageIcon(getClass().getResource(bundle.getString("ICON_Transient")));
            objectIcon = new ImageIcon(getClass().getResource(bundle.getString("ICON_Object")));

            defaultColor = getForeground();
            serviceColor = ColorUtilities.hexStringToColor( bundle.getString("COLOR_Service") );
            entityColor = ColorUtilities.hexStringToColor( bundle.getString("COLOR_Entity") );
            valueColor = ColorUtilities.hexStringToColor( bundle.getString("COLOR_Value") );
            transientColor = ColorUtilities.hexStringToColor( bundle.getString("COLOR_Transient") );
            objectColor = ColorUtilities.hexStringToColor( bundle.getString("COLOR_Object") );

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final Component getTreeCellRendererComponent(
        JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus )
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
        Object userObject = treeNode.getUserObject();

        // determine icon
        Icon icon = null;
        Color color = defaultColor;

        if (userObject instanceof ApplicationDetailDescriptor)
        {
            icon = applicationIcon;
        } else if (userObject instanceof LayerDetailDescriptor ) {
            icon = layerIcon;
        } else if (userObject instanceof ModuleDetailDescriptor ) {
            icon = moduleIcon;
        } else if (userObject instanceof ServiceDetailDescriptor ) {
            icon = serviceIcon;
            color = serviceColor;            
        } else if (userObject instanceof EntityDetailDescriptor ) {
            icon = entityIcon;
            color = entityColor;
        } else if (userObject instanceof ValueDetailDescriptor ) {
            icon = valueIcon;
            color = valueColor;
        } else if (userObject instanceof CompositeDetailDescriptor ) {
            icon = transientIcon;
            color = transientColor;
        } else if (userObject instanceof ObjectDetailDescriptor ) {
            icon = objectIcon;
            color = objectColor;
        } else {
            // assume it is string
            String str = userObject.toString();
            if (str.equalsIgnoreCase( "Services" ))
            {
                icon = serviceIcon;
                color = serviceColor;
            } else if (str.equalsIgnoreCase( "Entities" )) {
                icon = entityIcon;
                color = entityColor;
            } else if (str.equalsIgnoreCase( "Values" )) {
                icon = valueIcon;
                color = valueColor;
            } else if (str.equalsIgnoreCase( "Transients" )) {
                icon = transientIcon;
                color = transientColor;
            } else if (str.equalsIgnoreCase( "Objects" )) {
                icon = objectIcon;
                color = objectColor;
            }
        }


        if (icon != null) { setIcon( icon ); }
        /*if (!selected)
        {
            setForeground( color );
        }*/

        return this;
    }

}
