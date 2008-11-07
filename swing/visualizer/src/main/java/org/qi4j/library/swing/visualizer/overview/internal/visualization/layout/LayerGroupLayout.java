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
import java.util.Collections;
import java.util.List;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class LayerGroupLayout
    extends AbstractContainerLayout<LayerLayout>
{
    private final List<LayerLayout> layers;

    LayerGroupLayout( List<LayerLayout> layers )
    {
        Collections.sort( layers, LABEL_COMPARATOR );
        this.layers = layers;
    }

    protected final Iterable<LayerLayout> children()
    {
        return layers;
    }

    protected final int childrenCount()
    {
        return layers.size();
    }

    public final Rectangle2D applyLayout( LayoutConstraint constraint )
    {
        return arrangeChildrenHorizontally( constraint );
    }

    public final Dimension preferredDimension()
    {
        return preferredDimensionIfChildrenArrangedHorizontally();
    }
}
