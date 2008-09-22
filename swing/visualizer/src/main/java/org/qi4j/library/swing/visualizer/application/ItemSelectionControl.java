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
package org.qi4j.library.swing.visualizer.application;

import java.awt.event.MouseEvent;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.library.swing.visualizer.common.GraphConstants;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.NodeType;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 * @author edward.yakop@gmail.com
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

    public final void itemClicked( VisualItem anItem, MouseEvent anEvent )
    {
        NodeType selectedNode = (NodeType) anItem.get( FIELD_TYPE );
        Object descriptor = anItem.get( GraphConstants.FIELD_DESCRIPTOR );
        switch( selectedNode )
        {
        case APPLICATION:
            ApplicationDescriptor appDesc = (ApplicationDescriptor) descriptor;
            listener.onSelected( appDesc );
            break;
        case COMPOSITE:
            CompositeDescriptor compDesc = (CompositeDescriptor) descriptor;
            listener.onSelected( compDesc );
            break;
        case LAYER:
            LayerDescriptor layerDesc = (LayerDescriptor) descriptor;
            listener.onSelected( layerDesc );
            break;
        case MODULE:
            ModuleDescriptor moduleDesc = (ModuleDescriptor) descriptor;
            listener.onSelected( moduleDesc );
            break;
        }
    }
}