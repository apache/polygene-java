/*
 * Copyright 2008 Edward Yakop. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.swing.visualizer.overview.internal.visualization.layout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_BOTTOM;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_LEFT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_RIGHT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_TOP;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.vSpace;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ModuleArtifactGroupBoundsComputer
    extends AbstractBoundsComputer
{
    public final Rectangle computeBounds( NodeItem node1, Point location )
    {
        Dimension dimension = getNodeLabelSize( node1 );

        int moduleArtifactPosX = location.x + PADDING_LEFT;
        int moduleArtifactPosY = location.y + PADDING_TOP + dimension.height + vSpace;

        Map<NodeItem, Rectangle> moduleArtifacts = new HashMap<NodeItem, Rectangle>();
        int moduleArtifactGroupWidth = 0;
        Iterator children = node1.children();
        while( children.hasNext() )
        {
            NodeItem moduleArtifactNode = (NodeItem) children.next();
            Rectangle bounds =
                computeModuleArtifactBounds( moduleArtifactNode, moduleArtifactPosX, moduleArtifactPosY );
            moduleArtifacts.put( moduleArtifactNode, bounds );

            moduleArtifactPosY += bounds.height + PADDING_BOTTOM;
            if( bounds.width > moduleArtifactGroupWidth )
            {
                moduleArtifactGroupWidth = bounds.width;
            }
        }

        if( moduleArtifactGroupWidth < dimension.width )
        {
            moduleArtifactGroupWidth = dimension.width;
        }

        // Update all module artifact bounds to have the same width
        Set<Map.Entry<NodeItem, Rectangle>> entries = moduleArtifacts.entrySet();
        for( Map.Entry<NodeItem, Rectangle> entry : entries )
        {
            NodeItem moduleArtifact = entry.getKey();
            Rectangle moduleBounds = entry.getValue();

            moduleArtifact.setBounds( moduleBounds.x, moduleBounds.y, moduleArtifactGroupWidth, moduleBounds.height );
        }

        if( moduleArtifactPosY < location.y + dimension.height )
        {
            moduleArtifactPosY = location.y + dimension.height;
        }

        int width = ( moduleArtifactPosX + moduleArtifactGroupWidth + PADDING_RIGHT ) - location.x;
        int height = ( moduleArtifactPosY + PADDING_BOTTOM ) - location.y;
        return new Rectangle( location.x, location.y, width, height );
    }

    private Rectangle computeModuleArtifactBounds( NodeItem composite, int moduleArtifactXPos, int moduleArtifactYPos )
    {
        Dimension dimension = getNodeLabelSize( composite );
        int artifactWidth = dimension.width + PADDING_LEFT;
        int artifactHeight = dimension.height + PADDING_TOP + PADDING_BOTTOM;
        return new Rectangle( moduleArtifactXPos, moduleArtifactYPos, artifactWidth, artifactHeight );
    }
}
