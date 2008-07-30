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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Collection;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.render.Renderer;
import prefuse.visual.NodeItem;
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
        Point topLeft = new Point( 100, 100 );
        Rectangle size = computeApplicationBounds( root, topLeft );
        root.setBounds( topLeft.x, topLeft.y, size.width, size.height );
    }

    Collection<Set<NodeItem>> resolveLayerDependencies( Iterator nodes )
    {
        TreeMap<Integer, Set<NodeItem>> map = new TreeMap<Integer, Set<NodeItem>>();
        while( nodes.hasNext() )
        {
            NodeItem layer = (NodeItem) nodes.next();
            int level = layer.getInt( FIELD_LAYER_LEVEL );
            Set<NodeItem> set = map.get( level );
            if( set == null )
            {
                set = new HashSet<NodeItem>();
                map.put( level, set );
            }
            set.add( layer );
        }

        return map.values();
    }

    private Rectangle computeApplicationBounds( NodeItem application, Point location )
    {

        Dimension dimesion = getNodeLabelSize( application );
        int x = location.x + paddingLeft;
        int y = location.y + paddingTop + dimesion.height + vSpace;

        Collection<Set<NodeItem>> layeredNodeGroups = resolveLayerDependencies( application.children() );

        int maxLayerGroupWidth = 0;
        for( Set<NodeItem> nodeGroup : layeredNodeGroups )
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

        Rectangle bounds = new Rectangle( location.x, location.y, width, height );
//        System.out.println( getName( application ) + " - " + bounds );
        return bounds;
    }

    private Rectangle computeLayerGroupBounds( Set<NodeItem> layers, Point location )
    {
        int x = location.x + paddingLeft;
//        int y = location.y + paddingTop + vSpace;
        int y = location.y + vSpace;

        int maxLayerHeight = 0;
        for( NodeItem layer : layers )
        {

            Point layerLocation = new Point( x, y );
            Rectangle bounds = computeLayerBounds( layer, layerLocation );
            layer.setBounds( bounds.x, bounds.y, bounds.width, bounds.height );

            x += bounds.width + hSpace;
            if( bounds.height > maxLayerHeight )
            {
                maxLayerHeight = bounds.height;
            }

        }

        int width = x - location.x;
        int height = ( y + maxLayerHeight + paddingBottom ) - location.y;

        Rectangle bounds = new Rectangle( location.x, location.y, width, height );
//        System.out.println( getName( application ) + " - " + bounds );
        return bounds;
    }

    private Rectangle computeLayerBounds( NodeItem layer, Point location )
    {
        // TODO; Now placed side-by-side. Should possibly allow more clever layout.

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
        int height = ( y + maxModuleHeight + paddingBottom ) - location.y;
        Rectangle bounds = new Rectangle( location.x, location.y, width, height );
//        System.out.println( getName( layer ) + " - " + bounds );
        return bounds;
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
        Rectangle bounds = new Rectangle( location.x, location.y, width, height );
//        System.out.println( getName( module ) + " - " + bounds );
        return bounds;
    }

    private Rectangle computeCompositeBounds( NodeItem composite, Point location )
    {
        Dimension dimension = getNodeLabelSize( composite );
        Rectangle bounds = new Rectangle( location.x, location.y, dimension.width + paddingLeft,
                                          dimension.height + paddingTop + paddingBottom );
//        System.out.println( getName( composite ) + " - " + bounds );
        return bounds;
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
