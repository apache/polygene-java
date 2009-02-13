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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_LAYER_LEVEL;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ApplicationLayout extends AbstractContainerLayout<LayerGroupLayout>
{
    private final List<LayerGroupLayout> layerGroups;

    ApplicationLayout( NodeItem anApplicationNode )
        throws IllegalArgumentException
    {
        super( anApplicationNode );

        layerGroups = new ArrayList<LayerGroupLayout>();
        Collection<List<LayerLayout>> groups = groupLayers();
        for( List<LayerLayout> group : groups )
        {
            layerGroups.add( new LayerGroupLayout( group ) );
        }
    }

    private Collection<List<LayerLayout>> groupLayers()
    {
        TreeMap<Integer, List<LayerLayout>> layerGroupMaps = new TreeMap<Integer, List<LayerLayout>>();

        Iterator nodes = nodeItem.children();
        while( nodes.hasNext() )
        {
            NodeItem layer = (NodeItem) nodes.next();

            LayerLayout layerLayout = new LayerLayout( layer );

            int level = layer.getInt( FIELD_LAYER_LEVEL );
            List<LayerLayout> group = layerGroupMaps.get( level );

            if( group == null )
            {
                group = new LinkedList<LayerLayout>();
                layerGroupMaps.put( level, group );
            }
            group.add( layerLayout );
        }

        return layerGroupMaps.values();
    }

    protected Iterable<LayerGroupLayout> children()
    {
        return layerGroups;
    }

    protected int childrenCount()
    {
        return layerGroups.size();
    }

    public Rectangle2D applyLayout( LayoutConstraint constraint )
    {
        return arrangeChildrenVertically( constraint );
    }

    public Dimension preferredDimension()
    {
        return preferredDimensionIfChildrenArrangedVertically();

        /*Dimension size = preferredDimensionIfChildrenArrangedVertically();
        int width = size.width;
        int height = size.height;

        double scale = nodeItem.getVisualization().getDisplay( 0 ).getScale();
        if( scale < 1 )
        {
            double sWidth = width * scale;
            double sHeight = height * scale;

            if(  (width - sWidth ) >= (PADDING_LEFT + PADDING_RIGHT)
                || (height - sHeight) >= (PADDING_TOP + PADDING_BOTTOM) )
            {

                size = new Dimension( 0, 0);
                //System.out.println(nodeItem.getString( "name" ));

            }
        }

        return size;
        */
    }
}
