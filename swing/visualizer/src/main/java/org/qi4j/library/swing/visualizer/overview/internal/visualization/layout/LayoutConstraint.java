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
import java.awt.Point;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class LayoutConstraint
{
    private final Point topLeftHandCorner;
    private final Dimension size;

    public LayoutConstraint( Point topLeftHandCorner, Dimension size )
        throws IllegalArgumentException
    {
        validateNotNull( "topLeftHandCorner", topLeftHandCorner );
        this.topLeftHandCorner = topLeftHandCorner;
        this.size = size;
    }

    /**
     * @return Top land hand corner position.
     * @since 0.5
     */
    public final Point topLeftHandCorner()
    {
        return topLeftHandCorner;
    }

    /**
     * @return size constraint. Return {@code null} if there is no constraint.
     * @since 0.5
     */
    public final Dimension size()
    {
        return size;
    }

    @Override public String toString()
    {
        return "LayoutConstraint{" +
               "topLeftHandCorner=" + topLeftHandCorner +
               ", size=" + size +
               '}';
    }
}
