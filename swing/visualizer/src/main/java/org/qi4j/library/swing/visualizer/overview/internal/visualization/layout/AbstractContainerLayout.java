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
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import static java.lang.Math.max;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_BOTTOM;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_LEFT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_RIGHT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_TOP;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.hSpace;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.vSpace;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
abstract class AbstractContainerLayout<V extends AbstractLayout> extends AbstractLayout
{
    protected AbstractContainerLayout( NodeItem aNodeItem )
        throws IllegalArgumentException
    {
        super( aNodeItem );
    }

    protected AbstractContainerLayout()
    {
        super();
    }

    /**
     * @return Children. Must not return {@code null}.
     */
    protected abstract Iterable<V> children();

    /**
     * @return Children count.
     */
    protected abstract int childrenCount();

    protected final Dimension maximumChildPreferredDimension()
    {
        Iterable<V> children = children();
        int maxChildWidth = Integer.MIN_VALUE;
        int maxChildHeight = Integer.MIN_VALUE;
        for( V child : children )
        {
            Dimension dimension = child.preferredDimension();
            maxChildWidth = (int) max( maxChildWidth, dimension.getWidth() );
            maxChildHeight = (int) max( maxChildHeight, dimension.getHeight() );
        }

        return new Dimension( maxChildWidth, maxChildHeight );
    }

    protected final Rectangle2D arrangeChildrenHorizontally( LayoutConstraint aConstraint )
    {
        // label
        Dimension labelDimension = labelDimension();

        Point progress = computeLabel( aConstraint, labelDimension );
        int xPos = progress.x;
        int yPos = progress.y;

        // Children
        int childrenCount = childrenCount();
        if( childrenCount > 0 )
        {
            if( labelDimension != null )
            {
                yPos += vSpace;
            }

            // Work out dimension
            Dimension maximumChildPreferredDimension = maximumChildPreferredDimension();
            double childHeight = maximumChildPreferredDimension.getHeight();

            Iterable<V> children = children();
            for( V child : children )
            {
                Point childLocation = new Point( xPos, yPos );
                Dimension sizeConstraint = child.preferredDimension();
                double childWidth = sizeConstraint.getWidth();
                sizeConstraint.setSize( childWidth, childHeight );
                LayoutConstraint constraint = new LayoutConstraint( childLocation, sizeConstraint );

                child.applyLayout( constraint );
                xPos += childWidth + hSpace;
            }
            xPos -= hSpace;
            yPos += childHeight;
        }

        return updateBounds( aConstraint, labelDimension, xPos, yPos );
    }

    private Point computeLabel( LayoutConstraint aConstraint, Dimension labelDimension )
    {
        Point topLeftHandCorner = aConstraint.topLeftHandCorner();

        int topLeftHandCornerX = topLeftHandCorner.x;
        int topLeftHandCornerY = topLeftHandCorner.y;

        int xPos = topLeftHandCornerX + PADDING_LEFT;
        int yPos = topLeftHandCornerY + PADDING_TOP;
        if( labelDimension != null )
        {
            yPos += labelDimension.height;
        }

        return new Point( xPos, yPos );
    }

    private Rectangle2D updateBounds( LayoutConstraint aConstraint, Dimension labelDimension, int xPos, int yPos )
    {
        Point topLeftHandCorner = aConstraint.topLeftHandCorner();
        int topLeftHandCornerX = topLeftHandCorner.x;
        int topLeftHandCornerY = topLeftHandCorner.y;

        Dimension sizeConstraint = aConstraint.size();
        int labelWidth = computeWidth( labelDimension );
        int labelTopRightXPosition = topLeftHandCornerX + PADDING_LEFT + labelWidth;
        int topRightHandCornerWithNoPadding = max( xPos, labelTopRightXPosition );
        int width = topRightHandCornerWithNoPadding - topLeftHandCornerX + PADDING_RIGHT;
        width = (int) max( width, sizeConstraint.getWidth() );
        int height = yPos - topLeftHandCornerY + PADDING_BOTTOM;
        height = (int) max( height, sizeConstraint.getHeight() );

        // Update bounds
        Rectangle2D bounds;
        if( nodeItem != null )
        {
            bounds = nodeItem.getBounds();
            nodeItem.setBounds( topLeftHandCornerX, topLeftHandCornerY, width, height );
        }
        else
        {
            bounds = new Rectangle( topLeftHandCornerX, topLeftHandCornerY, width, height );
        }
        return bounds;
    }

    private int computeWidth( Dimension dimension )
    {
        if( dimension != null )
        {
            return dimension.width;
        }

        return 0;
    }


    protected final Rectangle2D arrangeChildrenVertically( LayoutConstraint aConstraint )
    {
        Dimension labelDimension = labelDimension();
        int labelWidth = computeWidth( labelDimension );

        Point progress = computeLabel( aConstraint, labelDimension );
        int xPos = progress.x;
        int yPos = progress.y;

        // Children
        int childrenCount = childrenCount();
        if( childrenCount > 0 )
        {
            if( labelDimension != null )
            {
                yPos += vSpace;
            }

            Dimension maximumChildPreferredDimension = maximumChildPreferredDimension();
            int childWidth = (int) max( labelWidth, maximumChildPreferredDimension.getWidth() );

            double childHeight = maximumChildPreferredDimension.getHeight();
            maximumChildPreferredDimension.setSize( childWidth, childHeight );

            Iterable<V> children = children();
            for( V child : children )
            {
                Point childLocation = new Point( xPos, yPos );
                LayoutConstraint constraint = new LayoutConstraint( childLocation, maximumChildPreferredDimension );

                child.applyLayout( constraint );
                yPos += ( childHeight + vSpace );
            }

            xPos += childWidth;
            yPos -= vSpace;
        }

        // Dimension
        return updateBounds( aConstraint, labelDimension, xPos, yPos );
    }


    protected final Dimension preferredDimensionIfChildrenArrangedHorizontally()
    {
        Dimension labelDimension = labelDimension();
        int labelHeight = ( labelDimension == null ) ? 0 : (int) labelDimension.getHeight();
        int height = PADDING_TOP + labelHeight;
        int width = PADDING_LEFT;

        int childrenCount = childrenCount();
        if( childrenCount > 0 )
        {
            Dimension maximumChildPreferredDimension = maximumChildPreferredDimension();

            double childwidth = maximumChildPreferredDimension.getWidth();
            width += ( childrenCount * ( childwidth + hSpace ) ) - hSpace;

            if( labelDimension != null )
            {
                height += vSpace;
            }
            height += maximumChildPreferredDimension.getHeight();
        }

        // Compare with Label width and child width
        int labelWidth = ( labelDimension == null ) ? 0 : (int) labelDimension.getWidth();
        int labelTopRightHandCornerX = PADDING_LEFT + labelWidth;
        width = max( width, labelTopRightHandCornerX );
        width += PADDING_RIGHT;
        height += PADDING_BOTTOM;

        return new Dimension( width, height );
    }

    protected final Dimension preferredDimensionIfChildrenArrangedVertically()
    {
        Dimension labelDimension = labelDimension();
        int labelHeight = ( labelDimension == null ) ? 0 : (int) labelDimension.getHeight();
        int height = PADDING_TOP + labelHeight;
        int width = PADDING_LEFT;

        int childrenCount = childrenCount();
        if( childrenCount > 0 )
        {
            Dimension maximumChildPreferredDimension = maximumChildPreferredDimension();

            double childwidth = maximumChildPreferredDimension.getWidth();
            width += childwidth;

            double childHeight = maximumChildPreferredDimension.getHeight();
            height += vSpace;
            height += ( childrenCount * ( childHeight + vSpace ) );
            if( labelDimension == null )
            {
                height -= vSpace;
            }
        }

        // Compare with Label width and child width
        int labelWidth = ( labelDimension == null ) ? 0 : (int) labelDimension.getWidth();
        int labelTopRightHandCornerX = PADDING_LEFT + labelWidth;
        width = max( width, labelTopRightHandCornerX );
        width += PADDING_RIGHT;
        height += PADDING_BOTTOM;

        return new Dimension( width, height );
    }
}
