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
package org.qi4j.library.swing.envisage.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import prefuse.data.Graph;

/**
 * Just a simple wrapper for ApplicationModelDisplay
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class GraphPane extends JPanel
{
    private GraphDisplay display;
    private BoxedGraphDisplay boxedDisplay;

    protected ApplicationDetailDescriptor descriptor;

    protected JTabbedPane tabPane;
    //protected JScrollPane

    public GraphPane()
    {
        display = new GraphDisplay();
        boxedDisplay = new BoxedGraphDisplay();
        //setBackground(display.getBackground());
        //setForeground(display.getForeground());
        //add(display, BorderLayout.CENTER);

        tabPane = new JTabbedPane( );
        tabPane.add("Tree", display);
        tabPane.add("Boxed", new JScrollPane(boxedDisplay));

        add(tabPane, BorderLayout.CENTER);

        this.addComponentListener( new ComponentAdapter()
        {
            public void componentResized( ComponentEvent evt)
            {
                Dimension size = GraphPane.this.getSize();
                display.setSize( size.width, size.height );

                tabPane.revalidate();
                
                tabPane.repaint();
                //display.repaint();
                //boxedDisplay.repaint();

                //repaint();
            }
        });
    }

    public void initQi4J( ApplicationDetailDescriptor descriptor)
    {
        this.descriptor = descriptor;

        Graph graph = GraphBuilder.buildGraph( descriptor );
        Dimension size = getSize();
        display.setSize( size.width, size.height );
        display.run(graph);

        graph = BoxedGraphBuilder.buildGraph( descriptor );
        boxedDisplay.setSize( size.width, size.height );
        boxedDisplay.run(graph);
    }

    public void refresh()
    {
        display.run();
    }

    public GraphDisplay getGraphDisplay()
    {
        return display;
    }
}
