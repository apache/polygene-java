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

package org.qi4j.library.framework.swing;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.render.Renderer;
import prefuse.visual.NodeItem;
import prefuse.data.Node;
import static org.qi4j.library.framework.swing.GraphConstants.*;

public class ApplicationLayout extends TreeLayout
{

    public ApplicationLayout( String group )
    {
        super( group );
    }

    public void run( double frac )
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

    private Rectangle computeApplicationBounds( NodeItem application, Point location )
    {
        Dimension dimesion = getNodeLabelSize( application );
        int x = location.x + paddingLeft;
        int y = location.y + paddingTop + dimesion.height + vSpace;

        Collection<Collection<NodeItem>> layeredNodeGroups = resolveLayerDependencies( application.children() );

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

        int width = ( x + maxLayerGroupWidth + paddingRight ) - location.x;
        int height = y - location.y;

        return new Rectangle( location.x, location.y, width, height );
    }

    private Map<Integer, NodeItem> nodeToVisualNodeItemMap = new HashMap<Integer, NodeItem>();

    /**
     * Tries to suggest a suitable x position and width, if there is only 1 layer in this group.
     * The position and width is based on the layers in higher group that use this layer
     */
    private Rectangle getSuggestedBounds( Collection<NodeItem> layers, NodeItem layer, int x, int y )
    {
        // todo - try to find a better way of getting the node associated with the visual node item
        nodeToVisualNodeItemMap.put( layer.getRow(), layer );

        Collection<Node> usedByLayers = (Collection<Node>) layer.get( FIELD_USED_BY_LAYERS );
        if( usedByLayers.isEmpty() || layers.size() > 1 )
        {
            return new Rectangle( x, y, 0, 0 );
        }

        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int width = 0;
        for( Node usedByLayer : usedByLayers )
        {
            NodeItem item = nodeToVisualNodeItemMap.get( usedByLayer.getRow() );
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
        return new Rectangle( left, y, width, 0 );
    }

    private Rectangle computeLayerGroupBounds( Collection<NodeItem> layers, Point location )
    {
        int x = location.x + paddingLeft;
        int y = location.y + vSpace;

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

        int width = x - location.x;
        int height = ( y + maxLayerHeight + paddingBottom ) - location.y;

        return new Rectangle( location.x, location.y, width, height );
    }

    private Rectangle computeLayerBounds( NodeItem layer, Point location )
    {
        Dimension dimension = getNodeLabelSize( layer );
        int x = location.x + paddingLeft;
        int y = location.y + paddingTop + dimension.height + vSpace;

        Iterator children = layer.children();
        int maxModuleHeight = 0;
        while( children.hasNext() )
        {
            NodeItem module = (NodeItem) children.next();
            Point moduleLocation = new Point( x, y );
            Rectangle bounds = computeModuleBounds( module, moduleLocation );
            module.setBounds( bounds.x, bounds.y, bounds.width, bounds.height );

            x += bounds.width + hSpace;
            if( bounds.height > maxModuleHeight )
            {
                maxModuleHeight = bounds.height;
            }

        }

        if( x < location.x + dimension.width )
        {
            x = location.x + dimension.width;
        }

        int width = x - location.x;
        int height = ( y + maxModuleHeight + paddingBottom + vSpace ) - location.y;

        return new Rectangle( location.x, location.y, width, height );
    }

    private Rectangle computeModuleBounds( NodeItem module, Point location )
    {

        Dimension dimension = getNodeLabelSize( module );
        int x = location.x + paddingLeft;
        int y = location.y + paddingTop + dimension.height + vSpace;

        Iterator children = module.children();
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
}
