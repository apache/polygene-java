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
package org.qi4j.library.swing.visualizer.overview.internal.visualization.layout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_BOTTOM;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_LEFT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_RIGHT;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ModuleArtifactLayout
    extends AbstractLayout
{
    public ModuleArtifactLayout( NodeItem aModuleArtifact )
        throws IllegalArgumentException
    {
        super( aModuleArtifact );
    }

    public final Rectangle2D applyLayout( LayoutConstraint constraint )
    {
        Point topLeftHandCorner = constraint.topLeftHandCorner();

        Dimension dimension = constraint.size();
        if( dimension == null )
        {
            dimension = preferredDimension();
        }

        int x = topLeftHandCorner.x;
        int y = topLeftHandCorner.y;
        double width = dimension.getWidth();
        double height = dimension.getHeight();
        nodeItem.setBounds( x, y, width, height );

        return nodeItem.getBounds();
    }

    public final Dimension preferredDimension()
    {
        Dimension dimension = labelDimension();
        double width = dimension.getWidth() + PADDING_LEFT + PADDING_RIGHT;
        double height = dimension.getHeight() + PADDING_RIGHT + PADDING_BOTTOM;
        dimension.setSize( width, height );

        return dimension;
    }
}
