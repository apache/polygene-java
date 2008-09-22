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
package org.qi4j.library.swing.visualizer.overview.internal;

import java.awt.event.MouseEvent;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.library.swing.visualizer.overview.SelectionListener;
import org.qi4j.library.swing.visualizer.overview.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 * @author edward.yakop@gmail.com
 */
public final class ItemSelectionControl extends ControlAdapter
{
    private final SelectionListener listener;

    public ItemSelectionControl( SelectionListener aListener )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( "aListener", aListener );
        listener = aListener;
    }

    @Override
    public final void itemClicked( VisualItem anItem, MouseEvent anEvent )
    {
        NodeType selectedNode = (NodeType) anItem.get( FIELD_TYPE );
        Object descriptor = anItem.get( GraphConstants.FIELD_DESCRIPTOR );
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
            ServiceDescriptor srvDesc = (ServiceDescriptor) descriptor;
            listener.onServiceSelected( srvDesc );
            break;
        case OBJECT:
            ObjectDescriptor objDesc = (ObjectDescriptor) descriptor;
            listener.onObjectSelected( objDesc );
            break;
        }
    }
}