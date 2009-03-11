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

import prefuse.action.layout.graph.TreeLayout;
import prefuse.visual.NodeItem;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class BoxedLayout extends TreeLayout
{
    public BoxedLayout(String group) {
        super(group);
    }

    public void run(double frac) {
        // setup
        NodeItem root = getLayoutRoot();
        setItemSize(root);

        //Rectangle2D b = getLayoutBounds();
        /*m_r.setRect(b.getX(), b.getY(), b.getWidth()-1, b.getHeight()-1);

        // process size values
        computeAreas(root);

        // layout root node
        setX(root, null, 0);
        setY(root, null, 0);
        root.setBounds(0, 0, m_r.getWidth(), m_r.getHeight());

        // layout the tree
        updateArea(root, m_r);
        layout(root, m_r);
        */
    }

    protected void setItemSize(NodeItem root)
    {
        
    }
}
