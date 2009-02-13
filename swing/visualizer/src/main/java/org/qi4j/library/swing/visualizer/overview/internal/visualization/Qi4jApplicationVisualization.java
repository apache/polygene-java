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
package org.qi4j.library.swing.visualizer.overview.internal.visualization;

import java.util.Iterator;
import org.qi4j.library.swing.visualizer.model.descriptor.ApplicationDetailDescriptor;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.EDGE_HIDDEN;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.layout.Qi4jApplicationLayout;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.render.RendererFactory;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import static prefuse.data.expression.ComparisonPredicate.EQ;
import prefuse.data.expression.ObjectLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import static prefuse.util.ColorLib.gray;
import prefuse.visual.VisualItem;
import static prefuse.visual.VisualItem.FILLCOLOR;
import static prefuse.visual.VisualItem.STROKECOLOR;

/**
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class Qi4jApplicationVisualization extends Visualization
{
    private static final String ACTION_COLOR = "color";
    private static final String ACTION_LAYOUT = "layout";
    private static final String ACTION_REPAINT = "repaint";
    private static final String ACTION_HIDE_EDGES = "hideEdges";

    private static final String GROUP_GRAPH = "graph";

    private Graph graph;

    Qi4jApplicationVisualization()
    {
        setRendererFactory( new RendererFactory() );
        addProcessingActions();
    }

    /**
     * Repopulate visualization with the specified {@code aDescriptor}.
     *
     * @param aDescriptor Application descriptor.
     * @since 0.5
     */
    final void populate( ApplicationDetailDescriptor aDescriptor )
    {
        reset();

        Graph tGraph = new Graph( true );

        if( aDescriptor != null )
        {
            ApplicationGraphBuilder builder = new ApplicationGraphBuilder( aDescriptor );
            builder.populate( tGraph );
        }

        removeGroup( GROUP_GRAPH );
        add( GROUP_GRAPH, tGraph );

        int count = getDisplayCount();
        for( int i = 0; i < count; i++ )
        {
            Display display = getDisplay( i );
            display.setDamageRedraw( false );
        }
        graph = tGraph;
    }

    final void launch()
    {
        run( ACTION_COLOR );
        run( ACTION_LAYOUT );
        run( ACTION_HIDE_EDGES );
        run( ACTION_REPAINT );
    }

    public final void clearAllNodeSelections()
    {
        TupleSet focusGroup = getGroup( FOCUS_ITEMS );
        focusGroup.clear();
        repaint();
    }

    public final void selectNodeByName( String aNodeName )
    {
        String query = FIELD_NAME + " = '" + aNodeName + "'";
        Predicate predicate = (Predicate) ExpressionParser.parse( query );

        TupleSet focusGroup = getGroup( FOCUS_ITEMS );
        focusGroup.clear();
        Iterator iterator = items( predicate );
        while( iterator.hasNext() )
        {
            Object o = iterator.next();
            focusGroup.addTuple( (Tuple) o );
        }
        run( ACTION_COLOR );
    }

    /**
     * @return Application node item.
     */
    public final VisualItem getApplicationNodeItem()
    {
        if( graph != null )
        {
            Node applicationNode = graph.getNode( 0 );
            return getVisualItem( "graph.nodes", applicationNode );
        }
        else
        {
            return null;
        }
    }

    private void addProcessingActions()
    {
        ActionList color = establishColors();
        putAction( ACTION_COLOR, color );

        Qi4jApplicationLayout layout = new Qi4jApplicationLayout( GROUP_GRAPH );
        putAction( ACTION_LAYOUT, layout );

        putAction( ACTION_REPAINT, new RepaintAction() );
        putAction( ACTION_HIDE_EDGES, new HideHiddenEdges() );
    }

    private ActionList establishColors()
    {
        // color for edges
        ColorAction edgesStroke = new ColorAction( "graph.edges", STROKECOLOR, gray( 100 ) );
        ColorAction edgesFill = new ColorAction( "graph.edges", FILLCOLOR, gray( 100 ) );

        // an action list containing all color assignments
        ActionList color = new ActionList();
        color.add( edgesStroke );
        color.add( edgesFill );
        return color;
    }


    private static class HideHiddenEdges extends Action
    {
        @Override
        public final void run( double frac )
        {
            ComparisonPredicate edgePredicate = new ComparisonPredicate(
                EQ, new ColumnExpression( FIELD_TYPE ), new ObjectLiteral( EDGE_HIDDEN )
            );

            Iterator itr = m_vis.items( "graph.edges", edgePredicate );
            while( itr.hasNext() )
            {
                VisualItem item = (VisualItem) itr.next();
                item.setVisible( false );
            }
        }
    }
}
