/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 * Copyright 2008 Sonny Gill. All Rights Reserved.
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
package org.qi4j.library.swing.visualizer.overview.internal.visualization;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_USED_BY_LAYERS;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.hSpace;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.paddingBottom;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.paddingLeft;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.paddingRight;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.paddingTop;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.vSpace;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.data.Node;
import prefuse.render.Renderer;
import prefuse.visual.NodeItem;

final class Qi4jApplicationLayout extends TreeLayout
{
    Qi4jApplicationLayout( String aGroup )
    {
        super( aGroup );
    }

    @Override
    public final void run( double frac )
    {
        NodeItem root = getLayoutRoot();
        Point topLeft = new Point( 50, 50 );
        Rectangle size = computeApplicationBounds( root, topLeft );
        root.setBounds( topLeft.x, topLeft.y, size.width, size.height );
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

    private Rectangle computeApplicationBounds( NodeItem anApplicationItem, Point aLocation )
    {
        Dimension dimesion = getNodeLabelSize( anApplicationItem );
        int x = aLocation.x + paddingLeft;
        int y = aLocation.y + paddingTop + dimesion.height + vSpace;

        Collection<Collection<NodeItem>> layeredNodeGroups = resolveLayerDependencies( anApplicationItem.children() );

        int maxLayerGroupWidth = 0;
        for( Collection<NodeItem> nodeGroup : layeredNodeGroups )
        {
            Point layerGroupLocation = new Point( x, y );
            Rectangle bounds = computeLayerGroupBounds( nodeGroup, layerGroupLocation );

            y += bounds.height + vSpace;
            if( bounds.width > maxLayerGroupWidth )
            {
                maxLayerGroupWidth = bounds.width;
            }
        }

        int width = ( x + maxLayerGroupWidth + paddingRight ) - aLocation.x;
        int height = y - aLocation.y;

        return new Rectangle( aLocation.x, aLocation.y, width, height );
    }

    private Map<Node, NodeItem> nodeToVisualNodeItemMap = new HashMap<Node, NodeItem>();

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

    private Rectangle computeLayerGroupBounds( Collection<NodeItem> layers, Point aLocation )
    {
        int x = aLocation.x + paddingLeft;
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
        int height = ( y + maxLayerHeight + paddingBottom ) - aLocation.y;

        return new Rectangle( aLocation.x, aLocation.y, width, height );
    }

    private Rectangle arrangeChildrenHorizontallyAndComputeBounds(
        NodeItem nodeItem, Point location, BoundsComputer childBoundsComputer )
    {
        Dimension dimension = getNodeLabelSize( nodeItem );
        int x = location.x + paddingLeft;
        int y = location.y + paddingTop + dimension.height + vSpace;

        Iterator children = nodeItem.children();
        int maxChildHeight = 0;
        while( children.hasNext() )
        {
            NodeItem child = (NodeItem) children.next();
            Point moduleLocation = new Point( x, y );
            Rectangle bounds = childBoundsComputer.computeBounds( child, moduleLocation );
            child.setBounds( bounds.x, bounds.y, bounds.width, bounds.height );

            x += bounds.width + hSpace;
            if( bounds.height > maxChildHeight )
            {
                maxChildHeight = bounds.height;
            }

        }

        if( x < location.x + dimension.width )
        {
            x = location.x + dimension.width;
        }

        int width = x - location.x;
        int height = ( y + maxChildHeight + paddingBottom + vSpace ) - location.y;

        return new Rectangle( location.x, location.y, width, height );
    }

    private Rectangle computeLayerBounds( NodeItem layer, Point location )
    {
        return arrangeChildrenHorizontallyAndComputeBounds( layer, location, new BoundsComputer()
        {
            public Rectangle computeBounds( NodeItem node, Point location )
            {
                return computeModuleBounds( node, location );
            }
        } );
    }

    private Rectangle computeModuleBounds( NodeItem module, Point location )
    {
        return arrangeChildrenHorizontallyAndComputeBounds( module, location, new BoundsComputer()
        {
            public Rectangle computeBounds( NodeItem node, Point location )
            {
                return createCompositeGroupBounds( node, location );
            }
        } );
    }

    private Rectangle createCompositeGroupBounds( NodeItem nodeItem, Point location )
    {

        Dimension dimension = getNodeLabelSize( nodeItem );
        int x = location.x + paddingLeft;
        int y = location.y + paddingTop + dimension.height + vSpace;

        Iterator children = nodeItem.children();
        int maxCompositeWidth = 0;
        while( children.hasNext() )
        {
            NodeItem composite = (NodeItem) children.next();
            Point compositeLocation = new Point( x, y );
            Rectangle bounds = computeCompositeBounds( composite, compositeLocation );
            composite.setBounds( bounds.x, bounds.y, bounds.width, bounds.height );

            y += bounds.height + paddingBottom;
            if( bounds.width > maxCompositeWidth )
            {
                maxCompositeWidth = bounds.width;
            }

        }

        if( maxCompositeWidth < dimension.width )
        {
            maxCompositeWidth = dimension.width;
        }

        if( y < location.y + dimension.height )
        {
            y = location.y + dimension.height;
        }

        int width = ( x + maxCompositeWidth + paddingRight ) - location.x;
        int height = ( y + paddingBottom ) - location.y;
        return new Rectangle( location.x, location.y, width, height );
    }

    private Rectangle computeCompositeBounds( NodeItem composite, Point location )
    {
        Dimension dimension = getNodeLabelSize( composite );
        return new Rectangle( location.x, location.y, dimension.width + paddingLeft,
                              dimension.height + paddingTop + paddingBottom );
    }

    private String getName( NodeItem node )
    {
        return (String) node.get( FIELD_NAME );
    }

    private Dimension getNodeLabelSize( NodeItem node )
    {
        Font font = node.getFont();
        FontMetrics fm = Renderer.DEFAULT_GRAPHICS.getFontMetrics( font );
        // 40 is arbitrarily selected, drawString takes more space than calculated here
        // this may be because the Graphics object is different from the one that is used to draw it
        int width = fm.stringWidth( getName( node ) ) + 40;
        int height = fm.getHeight() + 2;
        return new Dimension( width, height );
    }

    private interface BoundsComputer
    {
        public Rectangle computeBounds( NodeItem node, Point location );
    }
}
