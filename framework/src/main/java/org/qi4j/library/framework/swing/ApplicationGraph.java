/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.framework.swing;

import java.awt.Color;
import java.awt.event.WindowStateListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleModel;

/**
 * TODO
 */
public class ApplicationGraph
{
    public void show( ApplicationModel applicationModel )
    {
        GraphModel model = new DefaultGraphModel();
        final GraphLayoutCache view = new GraphLayoutCache( model, new DefaultCellViewFactory() );
        JGraph graph = new JGraph( model, view );

        final DefaultGraphCell applicationCell = new DefaultGraphCell( applicationModel.name() );

        applicationModel.visitModel( new ModelVisitor()
        {
            Map<LayerModel, DefaultGraphCell> layerCells = new HashMap<LayerModel, DefaultGraphCell>();

            private DefaultGraphCell layerCell;
            private DefaultGraphCell moduleCell;

            private int layerCount = 0;
            private int moduleCount = 0;

            @Override public void visit( ApplicationModel applicationModel )
            {
                AttributeMap attributes = applicationCell.getAttributes();
                GraphConstants.setHorizontalAlignment( attributes, SwingConstants.LEFT );
                GraphConstants.setVerticalAlignment( attributes, SwingConstants.TOP );
                GraphConstants.setInset( attributes, 30 );

                GraphConstants.setBounds( attributes, new Rectangle2D.Double( 100, 20, 300, 200 ) );
                GraphConstants.setGroupOpaque( attributes, true );
                GraphConstants.setOpaque( attributes, true );
                GraphConstants.setGradientColor( attributes, Color.yellow );
            }

            @Override public void visit( LayerModel layerModel )
            {
                layerCell = new DefaultGraphCell( layerModel.name() );
                layerCells.put( layerModel, layerCell );

                AttributeMap attributes = layerCell.getAttributes();
                GraphConstants.setHorizontalAlignment( attributes, SwingConstants.LEFT );
                GraphConstants.setVerticalAlignment( attributes, SwingConstants.TOP );
                GraphConstants.setInset( attributes, 30 );
                GraphConstants.setBounds( attributes, new Rectangle2D.Double( 20, 300 - ( 20 + layerCount * 100 ), 500, 80 ) );
                GraphConstants.setBackground( attributes, Color.green );
                GraphConstants.setGroupOpaque( attributes, true );
                GraphConstants.setOpaque( attributes, true );
                DefaultPort port0 = new DefaultPort();
                layerCell.add( port0 );

                applicationCell.add( layerCell );

                // Edges
                Iterable<LayerModel> usedLayers = layerModel.usedLayers().layers();
                for( LayerModel usedLayer : usedLayers )
                {
                    DefaultEdge edge = new DefaultEdge();
                    edge.setSource( layerCell.getChildAt( 0 ) );
                    edge.setTarget( layerCells.get( usedLayer ).getChildAt( 0 ) );
                    int arrow = GraphConstants.ARROW_CLASSIC;
                    GraphConstants.setLineEnd( edge.getAttributes(), arrow );
                    GraphConstants.setEndFill( edge.getAttributes(), true );
                    applicationCell.add( edge );
                }

                layerCount++;
                moduleCount = 0;
            }

            @Override public void visit( ModuleModel moduleModel )
            {
                moduleCell = new DefaultGraphCell( moduleModel.name() );

                AttributeMap attributes = moduleCell.getAttributes();
                //            GraphConstants.setInset( attributes, 30 );
                GraphConstants.setAutoSize( attributes, true );
                GraphConstants.setBackground( attributes, Color.blue );
                GraphConstants.setOpaque( attributes, true );
/*
                DefaultPort port0 = new DefaultPort();
                moduleCell.add( port0 );
*/

                layerCell.add( moduleCell );
                moduleCount++;
            }
        } );


        view.insert( applicationCell );
/*
        DefaultGraphCell[] cells = new DefaultGraphCell[3];
        cells[ 0 ] = new DefaultGraphCell( new String( "Hello" ) );
        GraphConstants.setBounds( cells[ 0 ].getAttributes(), new
            Rectangle2D.Double( 20, 20, 40, 20 ) );
        GraphConstants.setGradientColor(
            cells[ 0 ].getAttributes(),
            Color.orange );
        GraphConstants.setOpaque( cells[ 0 ].getAttributes(), true );
        DefaultPort port0 = new DefaultPort();
        cells[ 0 ].add( port0 );
        cells[ 1 ] = new DefaultGraphCell( new String( "World" ) );
        GraphConstants.setBounds( cells[ 1 ].getAttributes(), new
            Rectangle2D.Double( 140, 140, 40, 20 ) );
        GraphConstants.setGradientColor(
            cells[ 1 ].getAttributes(),
            Color.red );
        GraphConstants.setOpaque( cells[ 1 ].getAttributes(), true );
        DefaultPort port1 = new DefaultPort();
        cells[ 1 ].add( port1 );
        DefaultEdge edge = new DefaultEdge();
        edge.setSource( cells[ 0 ].getChildAt( 0 ) );
        edge.setTarget( cells[ 1 ].getChildAt( 0 ) );
        cells[ 2 ] = edge;
        int arrow = GraphConstants.ARROW_CLASSIC;
        GraphConstants.setLineEnd( edge.getAttributes(), arrow );
        GraphConstants.setEndFill( edge.getAttributes(), true );
        graph.getGraphLayoutCache().insert( cells );
*/

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.getContentPane().add( new JScrollPane( graph ) );
        frame.setSize( 600, 400 );
        frame.setVisible( true );
    }
}
