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
package org.qi4j.envisage.tree;

import java.awt.*;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.qi4j.tools.model.descriptor.*;

/**
 * TreeCellRenderer
 */
public class TreeModelCellRenderer
    extends DefaultTreeCellRenderer
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    protected Icon applicationIcon;
    protected Icon layerIcon;
    protected Icon moduleIcon;
    protected Icon serviceIcon;
    protected Icon importedServiceIcon;
    protected Icon entityIcon;
    protected Icon valueIcon;
    protected Icon transientIcon;
    protected Icon objectIcon;

    public TreeModelCellRenderer()
    {
        try
        {
            applicationIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Application" ) ) );
            layerIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Layer" ) ) );
            moduleIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Module" ) ) );
            serviceIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Service" ) ) );
            importedServiceIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_ImportedService" ) ) );
            entityIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Entity" ) ) );
            valueIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Value" ) ) );
            transientIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Transient" ) ) );
            objectIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Object" ) ) );
        }
        catch( Exception ex )
        {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public final Component getTreeCellRendererComponent(
        JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus
    )
    {
        super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Object userObject = treeNode.getUserObject();

        // determine icon
        Icon icon = null;

        if( userObject instanceof ApplicationDetailDescriptor )
        {
            icon = applicationIcon;
        }
        else if( userObject instanceof LayerDetailDescriptor )
        {
            icon = layerIcon;
        }
        else if( userObject instanceof ModuleDetailDescriptor )
        {
            icon = moduleIcon;
        }
        else if( userObject instanceof ServiceDetailDescriptor )
        {
            icon = serviceIcon;
        }
        else if( userObject instanceof ImportedServiceDetailDescriptor )
        {
            icon = importedServiceIcon;
        }
        else if( userObject instanceof EntityDetailDescriptor )
        {
            icon = entityIcon;
        }
        else if( userObject instanceof ValueDetailDescriptor )
        {
            icon = valueIcon;
        }
        else if( userObject instanceof CompositeDetailDescriptor )
        {
            icon = transientIcon;
        }
        else if( userObject instanceof ObjectDetailDescriptor )
        {
            icon = objectIcon;
        }
        else
        {
            // assume it is string
            String str = userObject.toString();
            if( str.equalsIgnoreCase( "Services" ) )
            {
                icon = serviceIcon;
            }
            else if( str.equalsIgnoreCase( "Imported Services" ) )
            {
                icon = importedServiceIcon;
            }
            else if( str.equalsIgnoreCase( "Entities" ) )
            {
                icon = entityIcon;
            }
            else if( str.equalsIgnoreCase( "Values" ) )
            {
                icon = valueIcon;
            }
            else if( str.equalsIgnoreCase( "Transients" ) )
            {
                icon = transientIcon;
            }
            else if( str.equalsIgnoreCase( "Objects" ) )
            {
                icon = objectIcon;
            }
        }

        if( icon != null )
        {
            setIcon( icon );
        }

        return this;
    }
}
