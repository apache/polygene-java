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

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.listener.SelectionListener;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ApplicationTreeSelectionListener
    implements TreeSelectionListener
{
    private final SelectionListener listener;

    ApplicationTreeSelectionListener( SelectionListener aListener )
        throws IllegalArgumentException
    {
        validateNotNull( "aListener", aListener );

        listener = aListener;
    }

    public final void valueChanged( TreeSelectionEvent e )
    {
        TreePath path = e.getPath();
        Object source = path.getLastPathComponent();

        if( source != null )
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) source;
            Object userObject = node.getUserObject();

            if( userObject == null )
            {
                return;
            }

            Class<?> userObjectClass = userObject.getClass();

            if( ApplicationDetailDescriptor.class.isAssignableFrom( userObjectClass ) )
            {
                ApplicationDetailDescriptor descriptor = (ApplicationDetailDescriptor) userObject;
                listener.onApplicationSelected( descriptor );
            }
            else if( ServiceDetailDescriptor.class.isAssignableFrom( userObjectClass ) )
            {
                ServiceDetailDescriptor detailDescriptor = (ServiceDetailDescriptor) userObject;
                listener.onServiceSelected( detailDescriptor );
            }
            else if( EntityDetailDescriptor.class.isAssignableFrom( userObjectClass ) )
            {
                EntityDetailDescriptor detailDescriptor = (EntityDetailDescriptor) userObject;
                listener.onEntitySelected( detailDescriptor );
            }
            else if( CompositeDetailDescriptor.class.isAssignableFrom( userObjectClass ) )
            {
                CompositeDetailDescriptor detailDescriptor = (CompositeDetailDescriptor) userObject;
                listener.onCompositeSelected( detailDescriptor );
            }
            else if( ObjectDetailDescriptor.class.isAssignableFrom( userObjectClass ) )
            {
                ObjectDetailDescriptor detailDescriptor = (ObjectDetailDescriptor) userObject;
                listener.onObjectSelected( detailDescriptor );
            }
            else if( LayerDetailDescriptor.class.isAssignableFrom( userObjectClass ) )
            {
                LayerDetailDescriptor detailDescriptor = (LayerDetailDescriptor) userObject;
                listener.onLayerSelected( detailDescriptor );
            }
            else if( ModuleDetailDescriptor.class.isAssignableFrom( userObjectClass ) )
            {
                ModuleDetailDescriptor detailDescriptor = (ModuleDetailDescriptor) userObject;
                listener.onModuleSelected( detailDescriptor );
            }
            else
            {
                listener.resetSelection();
            }
        }
    }
}
