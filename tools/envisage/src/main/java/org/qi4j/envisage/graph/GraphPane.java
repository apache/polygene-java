/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.envisage.graph;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import org.qi4j.envisage.event.LinkEvent;
import org.qi4j.envisage.event.LinkListener;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptor;
import prefuse.data.Graph;

/**
 * Just a simple wrapper for ApplicationModel Graph Display
 */
public class GraphPane
    extends JPanel
{
    private TreeGraphDisplay treeDisplay;
    private StackedGraphDisplay stackedDisplay;
    private List<GraphDisplay> displays;

    protected ApplicationDetailDescriptor descriptor;

    protected JTabbedPane tabPane;
    protected JScrollPane scrollPane;

    public GraphPane()
    {
        treeDisplay = new TreeGraphDisplay();
        stackedDisplay = new StackedGraphDisplay();

        List<GraphDisplay> tmpList = new ArrayList<GraphDisplay>( 2 );
        tmpList.add( treeDisplay );
        tmpList.add( stackedDisplay );
        displays = Collections.unmodifiableList( tmpList );

        scrollPane = new JScrollPane();
        scrollPane.setViewportView( stackedDisplay );
        int unitInc = 50;
        scrollPane.getVerticalScrollBar().setUnitIncrement( unitInc );
        scrollPane.getHorizontalScrollBar().setUnitIncrement( unitInc );

        tabPane = new JTabbedPane();
        tabPane.add( "Tree", treeDisplay );
        tabPane.add( "Stacked", scrollPane );

        this.setLayout( new BorderLayout() );
        add( tabPane, BorderLayout.CENTER );

        treeDisplay.addLinkListener( new LinkListener()
        {
            @Override
            public void activated( LinkEvent evt )
            {
                graphItemLinkActivated( evt );
            }
        } );

        stackedDisplay.addLinkListener( new LinkListener()
        {
            @Override
            public void activated( LinkEvent evt )
            {
                graphItemLinkActivated( evt );
            }
        } );

        this.addComponentListener( new ComponentAdapter()
        {
            @Override
            public void componentResized( ComponentEvent evt )
            {
                Dimension size = GraphPane.this.getSize();
                treeDisplay.setSize( size.width, size.height );
                tabPane.revalidate();
                tabPane.repaint();
            }
        } );
    }

    public void initQi4J( ApplicationDetailDescriptor descriptor )
    {
        this.descriptor = descriptor;

        Graph graph = GraphBuilder.buildGraph( descriptor );
        Dimension size = getSize();
        treeDisplay.setSize( size.width, size.height );
        treeDisplay.run( graph );

        graph = GraphBuilder.buildGraph( descriptor );
        stackedDisplay.setSize( size.width, size.height );
        stackedDisplay.run( graph );
    }

    public void refresh()
    {
        treeDisplay.run();
        stackedDisplay.run();
    }

    public List<GraphDisplay> getGraphDisplays()
    {
        return displays;
    }

    public void setSelectedValue( Object obj )
    {
        treeDisplay.setSelectedValue( obj );
        stackedDisplay.setSelectedValue( obj );
    }

    private void graphItemLinkActivated( LinkEvent evt )
    {
        //System.out.println("this is called");
        //System.out.println(evt.getSource().getClass());
        if( evt.getSource().equals( treeDisplay ) )
        {
            stackedDisplay.setSelectedValue( evt.getObject() );
        }
        else if( evt.getSource().equals( stackedDisplay ) )
        {
            treeDisplay.setSelectedValue( evt.getObject() );
        }
    }

    /**
     * Add a listener from the list that's notified each time a change to the selection occurs.
     *
     * @param listener the LinkListener to add
     */
    public void addLinkListener( LinkListener listener )
    {
        treeDisplay.addLinkListener( listener );
        stackedDisplay.addLinkListener( listener );
    }

    /**
     * Remove a listener from the list that's notified each time a change to the selection occurs.
     *
     * @param listener the LinkListener to remove
     */
    public void removeLinkListener( LinkListener listener )
    {
        treeDisplay.removeLinkListener( listener );
        stackedDisplay.removeLinkListener( listener );
    }
}
