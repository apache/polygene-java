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

import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.detailPanel.internal.common.form.DescriptorForm;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;

/**
 * TODO Javadoc
 * TODO selection back to overview panel
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ApplicationDetailPanel extends JSplitPane
{
    private static final Dimension PREFERRED_SIZE_LEFT_COMPONENT = new Dimension( 250, 300 );

    public ApplicationDetailPanel( ApplicationDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        // Right component
        DescriptorForm form = new DescriptorForm();
        JScrollPane rightComponent = new JScrollPane( form );
        setRightComponent( rightComponent );

        // Left component
        AppTree descriptorTree = createLeftComponent( aDescriptor, form );
        JScrollPane leftComponent = new JScrollPane( descriptorTree );
        leftComponent.setPreferredSize( PREFERRED_SIZE_LEFT_COMPONENT );
        setLeftComponent( leftComponent );
    }

    private AppTree createLeftComponent( ApplicationDetailDescriptor aDescriptor, DescriptorForm aForm )
    {
        AppTreeNode treeNode = new AppTreeNode( aDescriptor );
        AppTreeSelectionListener selectionListener = new AppTreeSelectionListener( aForm );
        return new AppTree( treeNode, selectionListener );
    }

    private static class AppTreeSelectionListener
        implements TreeSelectionListener
    {
        private final DescriptorForm form;

        private AppTreeSelectionListener( DescriptorForm aForm )
        {
            form = aForm;
        }

        public final void valueChanged( TreeSelectionEvent anEvent )
        {
            TreePath path = anEvent.getPath();

            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

            Object modelToDisplay = null;
            if( selectedNode != null )
            {
                modelToDisplay = selectedNode.getUserObject();
            }
            form.display( modelToDisplay );
        }
    }
}
