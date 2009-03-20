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

import org.qi4j.library.swing.envisage.event.LinkEvent;
import org.qi4j.library.swing.envisage.event.LinkListener;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Graph;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public abstract class GraphDisplay extends Display
{
    public static final String NAME_LABEL = "name";
    public static final String USER_OBJECT = "userObject";

    public static final String GRAPH = "graph";
    public static final String GRAPH_NODES = "graph.nodes";
    public static final String GRAPH_EDGES = "graph.edges";
    public static final String USES_EDGES = "uses.edges";

    public GraphDisplay( Visualization visualization)
    {
        super(visualization);        
    }

    public abstract void run ( Graph graph );
    public abstract void run ( );

    public abstract void setSelectedValue( Object object);

    /**
     * Add a listener that's notified each time a change to the selection occurs.
     *
     * @param listener the LinkListener to add
     */
    public void addLinkListener( LinkListener listener )
    {
        listenerList.add( LinkListener.class, listener );
    }

    /**
     * Remove a listener from the list that's notified each time a change to the selection occurs.
     *
     * @param listener the LinkListener to remove
     */
    public void removeLinkListener( LinkListener listener )
    {
        listenerList.remove( LinkListener.class, listener );
    }

    protected void fireLinkActivated( LinkEvent evt )
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for( int i = listeners.length - 2; i >= 0; i -= 2 )
        {
            if( listeners[ i ] == LinkListener.class )
            {
                ( (LinkListener) listeners[ i + 1 ] ).activated( evt );
            }
        }
    }
}
