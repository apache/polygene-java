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
package org.qi4j.library.swing.visualizer.overview.internal.visualization.layout;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class LayerLayout extends AbstractContainerLayout<ModuleLayout>
{
    private Set<ModuleLayout> modules;

    LayerLayout( NodeItem aLayer )
        throws IllegalArgumentException
    {
        super( aLayer );

        modules = new TreeSet<ModuleLayout>( LABEL_COMPARATOR );
        Iterator childrenIterator = aLayer.children();
        while( childrenIterator.hasNext() )
        {
            NodeItem moduleItem = (NodeItem) childrenIterator.next();
            modules.add( new ModuleLayout( moduleItem ) );
        }
    }

    protected final Iterable<ModuleLayout> children()
    {
        return modules;
    }

    protected final int childrenCount()
    {
        return modules.size();
    }

    public final Rectangle2D applyLayout( LayoutConstraint constraint )
    {
        return arrangeChildrenHorizontally( constraint );
        //return arrangeChildrenHorizontally( constraint, nodeItem.getVisualization().getDisplay( 0 ).getScale() );
    }

    public final Dimension preferredDimension()
    {
        return preferredDimensionIfChildrenArrangedHorizontally();
        //return preferredDimensionIfChildrenArrangedHorizontally(nodeItem.getVisualization().getDisplay( 0 ).getScale());
    }
}
