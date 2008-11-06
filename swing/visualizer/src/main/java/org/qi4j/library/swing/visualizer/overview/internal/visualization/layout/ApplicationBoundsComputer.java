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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_LAYER_LEVEL;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_USED_BY_LAYERS;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_BOTTOM;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_LEFT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_RIGHT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_TOP;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.hSpace;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.vSpace;
import prefuse.data.Node;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ApplicationBoundsComputer extends AbstractBoundsComputer
{
    private final Map<Node, NodeItem> nodeToVisualNodeItemMap;

    ApplicationBoundsComputer()
    {
        nodeToVisualNodeItemMap = new HashMap<Node, NodeItem>();
    }

    public final Rectangle computeBounds( NodeItem node, Point location )
    {
        Rectangle applicationBounds = computeApplicationBounds( node, location );
        node.setBounds( applicationBounds.x, applicationBounds.y, applicationBounds.width, applicationBounds.height );
        return applicationBounds;
    }

    private Rectangle computeApplicationBounds( NodeItem anApplicationItem, Point aLocation )
    {
        Dimension dimesion = getNodeLabelSize( anApplicationItem );
        int x = aLocation.x + PADDING_LEFT;
        int y = aLocation.y + PADDING_TOP + dimesion.height + vSpace;

        Collection<Collection<NodeItem>> layeredNodeGroups = resolveLayerDependencies( anApplicationItem.children() );

        int maxLayerGroupWidth = 0;
        for( Collection<NodeItem> nodeGroup : layeredNodeGroups )
        {
            Point layerGroupLocation = new Point( x, y );
            Rectangle bounds = computerApplicationBounds( nodeGroup, layerGroupLocation );

            y += bounds.height + vSpace;
            if( bounds.width > maxLayerGroupWidth )
            {
                maxLayerGroupWidth = bounds.width;
            }
        }

        int width = ( x + maxLayerGroupWidth + PADDING_RIGHT ) - aLocation.x;
        int height = y - aLocation.y;

        return new Rectangle( aLocation.x, aLocation.y, width, height );
    }

    Collection<Collection<NodeItem>> resolveLayerDependencies( Iterator nodes )
    {
        TreeMap<Integer, Collection<NodeItem>> map = new TreeMap<Integer, Collection<NodeItem>>();
        while( nodes.hasNext() )
        {
            NodeItem layer = (NodeItem) nodes.next();
            int level = layer.getInt( FIELD_LAYER_LEVEL );
            Collection<NodeItem> layers = map.get( level );
            if( layers == null )
            {
                layers = new ArrayList<NodeItem>();
                map.put( level, layers );
            }
            layers.add( layer );
        }

        return map.values();
    }

    private Rectangle computerApplicationBounds( Collection<NodeItem> layers, Point aLocation )
    {
        int x = aLocation.x + PADDING_LEFT;
        int y = aLocation.y + vSpace;

        int maxLayerHeight = 0;
        for( NodeItem layer : layers )
        {
            Rectangle suggestedBounds = getSuggestedBounds( layers, layer, x, y );
            Point layerLocation = new Point( suggestedBounds.x, y );

            Rectangle bounds = computeLayerBounds( layer, layerLocation );
            int width = Math.max( bounds.width, suggestedBounds.width );
            layer.setBounds( bounds.x, bounds.y, width, bounds.height );

            x += width + hSpace;
            if( bounds.height > maxLayerHeight )
            {
                maxLayerHeight = bounds.height;
            }
        }

        int width = x - aLocation.x;
        int height = ( y + maxLayerHeight + PADDING_BOTTOM ) - aLocation.y;

        return new Rectangle( aLocation.x, aLocation.y, width, height );
    }

    /**
     * Tries to suggest a suitable x position and width, if there is only 1 layer in this group.
     * The position and width is based on the layers in higher group that use this layer
     *
     * @param layers layers. This argument must not be {@code null}.
     * @param aLayer layer. This argument must not be {@code null}.
     * @param xLoc   x position.
     * @param yLoc   y position.
     * @return Suggested bounds.
     */
    @SuppressWarnings( "unchecked" )
    private Rectangle getSuggestedBounds( Collection<NodeItem> layers, NodeItem aLayer, int xLoc, int yLoc )
    {
        nodeToVisualNodeItemMap.put( (Node) aLayer.getSourceTuple(), aLayer );

        Collection<Node> usedByLayers = (Collection<Node>) aLayer.get( FIELD_USED_BY_LAYERS );
        if( usedByLayers.isEmpty() )
        {
            return new Rectangle( xLoc, yLoc, 0, 0 );
        }

        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int width;
        for( Node usedByLayer : usedByLayers )
        {
            NodeItem item = nodeToVisualNodeItemMap.get( usedByLayer );
            Rectangle2D bounds = item.getBounds();
            if( bounds.getX() < left )
            {
                left = (int) bounds.getX();
            }
            if( ( bounds.getX() + bounds.getWidth() ) > right )
            {
                right = (int) ( bounds.getX() + bounds.getWidth() );
            }
        }

        width = right - left;
        // Use a height of 1 instead of 0 for correct operation of Rectangle2D.intersects call later
        Rectangle suggestedBounds = new Rectangle( left, yLoc, width, 1 );

        // If there are other layers on this level, and the calculated suggested bounds intersect with
        // any layer's bounds, return the default bounds
        if( layers.size() > 1 )
        {
            Set<NodeItem> otherLayers = new HashSet<NodeItem>( layers );
            otherLayers.remove( aLayer );
            for( NodeItem otherLayer : otherLayers )
            {
                if( otherLayer.getBounds().intersects( suggestedBounds ) )
                {
                    return new Rectangle( xLoc, yLoc, 0, 0 );
                }
            }
        }

        return suggestedBounds;
    }

    private Rectangle computeLayerBounds( NodeItem layer, Point location )
    {
        return arrangeChildrenHorizontallyAndComputeBounds( layer, location, new ModuleBoundsComputer() );
    }
}
