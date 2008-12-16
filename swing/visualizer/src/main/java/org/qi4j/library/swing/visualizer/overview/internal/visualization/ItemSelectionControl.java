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
package org.qi4j.library.swing.visualizer.overview.internal.visualization;

import java.awt.event.MouseEvent;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.library.swing.visualizer.listener.SelectionListener;
import org.qi4j.library.swing.visualizer.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ServiceDetailDescriptor;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_DESCRIPTOR;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_TYPE;
import org.qi4j.library.swing.visualizer.overview.internal.common.NodeType;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ItemSelectionControl extends ControlAdapter
{
    private final SelectionListener listener;

    ItemSelectionControl( SelectionListener aListener )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( "aListener", aListener );
        listener = aListener;
    }

    @Override
    public final void itemClicked( VisualItem anItem, MouseEvent anEvent )
    {
        NodeType selectedNode = (NodeType) anItem.get( FIELD_TYPE );
        if( selectedNode == null )
        {
            return;
        }

        Object descriptor = anItem.get( FIELD_DESCRIPTOR );
        switch( selectedNode )
        {
        case APPLICATION:
            ApplicationDetailDescriptor appDesc = (ApplicationDetailDescriptor) descriptor;
            listener.onApplicationSelected( appDesc );
            break;
        case LAYER:
            LayerDetailDescriptor layerDesc = (LayerDetailDescriptor) descriptor;
            listener.onLayerSelected( layerDesc );
            break;
        case MODULE:
            ModuleDetailDescriptor moduleDesc = (ModuleDetailDescriptor) descriptor;
            listener.onModuleSelected( moduleDesc );
            break;
        case COMPOSITE:
            CompositeDetailDescriptor compDesc = (CompositeDetailDescriptor) descriptor;
            listener.onCompositeSelected( compDesc );
            break;
        case ENTITY:
            EntityDetailDescriptor entDesc = (EntityDetailDescriptor) descriptor;
            listener.onEntitySelected( entDesc );
            break;
        case SERVICE:
            ServiceDetailDescriptor srvDesc = (ServiceDetailDescriptor) descriptor;
            listener.onServiceSelected( srvDesc );
            break;
        case OBJECT:
            ObjectDetailDescriptor objDesc = (ObjectDetailDescriptor) descriptor;
            listener.onObjectSelected( objDesc );
            break;
        }
    }
}