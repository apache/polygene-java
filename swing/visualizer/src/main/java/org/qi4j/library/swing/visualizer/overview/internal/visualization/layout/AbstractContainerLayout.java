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
import static java.lang.Integer.MIN_VALUE;
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
    private static final LayoutConstraint BLANK_CONSTRAINT = new LayoutConstraint( new Point( 0, 0 ), new Dimension( 0, 0 ) );

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
        int maxChildWidth = MIN_VALUE;
        int maxChildHeight = MIN_VALUE;

        for( V child : children )
        {
            Dimension childDimension = child.preferredDimension();
            maxChildWidth = (int) max( maxChildWidth, childDimension.getWidth() );
            maxChildHeight = (int) max( maxChildHeight, childDimension.getHeight() );
        }

        return new Dimension( maxChildWidth, maxChildHeight );
    }

    // TODO: Follow dimension constraints
    protected final Rectangle2D arrangeChildrenHorizontally( LayoutConstraint aConstraint )
    {
        // label
        Dimension labelDimension = labelDimension();

        Point afterLabel = calculateLabel( aConstraint, labelDimension );
        int xPos = afterLabel.x;
        int yPos = afterLabel.y;

        // Children
        int childrenCount = childrenCount();
        if( childrenCount > 0 )
        {
            if( labelDimension != null )
            {
                yPos += vSpace;
            }
            Dimension childDimension = calculateChildDimensionIfArrangedHorizontally( aConstraint, labelDimension );

            Iterable<V> children = children();
            for( V child : children )
            {
                Point childLocation = new Point( xPos, yPos );
                LayoutConstraint constraint = new LayoutConstraint( childLocation, childDimension );

                Rectangle2D childBound = child.applyLayout( constraint );
                double childWidth = childBound.getWidth();
                xPos += childWidth + hSpace;
            }
            xPos -= hSpace;
            yPos += childDimension.getHeight();
        }

        return updateBounds( aConstraint, labelDimension, xPos, yPos );
    }

    private Dimension calculateChildDimensionIfArrangedHorizontally(
        LayoutConstraint aConstraint, Dimension labelDimension )
    {
        Dimension childDimension = maximumChildPreferredDimension();
        Dimension sizeConstraint = aConstraint.size();

        // calculate width
        int childrenCount = childrenCount();
        double maximumChildPreferedWidth = childDimension.getWidth();
        double widthWithNoPadding = sizeConstraint.getWidth() - PADDING_LEFT - PADDING_RIGHT;
        double childConstraintWidth = ( widthWithNoPadding - ( childrenCount * hSpace ) + hSpace ) / childrenCount;
        double childWidth = max( maximumChildPreferedWidth, childConstraintWidth );

        // Calculate height
        double heightConstraint = sizeConstraint.getHeight() - PADDING_TOP - PADDING_BOTTOM;
        if( labelDimension != null )
        {
            heightConstraint -= ( labelDimension.getHeight() + vSpace );
        }
        double childHeight = max( childDimension.getHeight(), heightConstraint );
        childDimension.setSize( childWidth, childHeight );

        return childDimension;
    }

    private Point calculateLabel( LayoutConstraint aConstraint, Dimension labelDimension )
    {
        Point topLeftHandCorner = aConstraint.topLeftHandCorner();

        int xPos = topLeftHandCorner.x + PADDING_LEFT;
        int yPos = topLeftHandCorner.y + PADDING_TOP;
        yPos += calculateHeight( labelDimension );

        return new Point( xPos, yPos );
    }

    private Rectangle2D updateBounds( LayoutConstraint aConstraint, Dimension labelDimension, int xPos, int yPos )
    {
        Point topLeftHandCorner = aConstraint.topLeftHandCorner();
        int topLeftHandCornerX = topLeftHandCorner.x;
        int topLeftHandCornerY = topLeftHandCorner.y;

        Dimension sizeConstraint = aConstraint.size();
        int labelWidth = calculateWidth( labelDimension );
        int labelTopRightXPosition = topLeftHandCornerX + PADDING_LEFT + labelWidth;
        int topRightHandCornerWithNoPadding = max( xPos, labelTopRightXPosition );
        int width = topRightHandCornerWithNoPadding - topLeftHandCornerX + PADDING_RIGHT;
        width = (int) max( width, sizeConstraint.getWidth() );
        int height = yPos - topLeftHandCornerY + PADDING_BOTTOM;
        height = (int) max( height, sizeConstraint.getHeight() );

        // Update bounds
        Rectangle2D bounds;
        if( nodeItem == null )
        {
            bounds = new Rectangle( topLeftHandCornerX, topLeftHandCornerY, width, height );
        }
        else
        {
            nodeItem.setBounds( topLeftHandCornerX, topLeftHandCornerY, width, height );
            bounds = nodeItem.getBounds();

        }

        return bounds;
    }

    private int calculateWidth( Dimension dimension )
    {
        if( dimension == null )
        {
            return 0;
        }

        return dimension.width;

    }

    private int calculateHeight( Dimension dimension )
    {
        if( dimension == null )
        {
            return 0;
        }

        return dimension.height;
    }

    // TODO: Follow dimension constraints
    protected final Rectangle2D arrangeChildrenVertically( LayoutConstraint aConstraint )
    {
        Dimension labelDimension = labelDimension();

        Point afterLabel = calculateLabel( aConstraint, labelDimension );
        int xPos = afterLabel.x;
        int yPos = afterLabel.y;

        // Children
        int childrenCount = childrenCount();
        if( childrenCount > 0 )
        {
            if( labelDimension != null )
            {
                yPos += vSpace;
            }

            double childWidth = calculateChildWidthIfArrangedVertically( aConstraint, labelDimension );

            Iterable<V> children = children();
            for( V child : children )
            {
                Point childLocation = new Point( xPos, yPos );
                Dimension childPreferredDimension = child.preferredDimension();
                int childHeight = (int) childPreferredDimension.getHeight();
                Dimension sizeConstraint = new Dimension( (int) childWidth, childHeight );
                LayoutConstraint constraint = new LayoutConstraint( childLocation, sizeConstraint );

                Rectangle2D childBounds = child.applyLayout( constraint );
                yPos += ( childBounds.getHeight() + vSpace );

            }

            xPos += childWidth;
            yPos -= vSpace;
        }

        // Dimension
        return updateBounds( aConstraint, labelDimension, xPos, yPos );
    }

    private double calculateChildWidthIfArrangedVertically(
        LayoutConstraint aConstraint, Dimension labelDimension )
    {
        Dimension childDimension = maximumChildPreferredDimension();
        int labelWidth = calculateWidth( labelDimension );
        int childWidth = (int) max( labelWidth, childDimension.getWidth() );
        Dimension sizeConstraint = aConstraint.size();
        double constraintWidth = sizeConstraint.getWidth() - PADDING_LEFT - PADDING_RIGHT;
        childWidth = (int) max( childWidth, constraintWidth );

        return childWidth;
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

        // Children
        int childrenCount = childrenCount();
        if( childrenCount > 0 )
        {
            if( labelDimension != null )
            {
                height += vSpace;
            }

            double childWidth = calculateChildWidthIfArrangedVertically( BLANK_CONSTRAINT, labelDimension );

            Iterable<V> children = children();
            for( V child : children )
            {
                Dimension childDimension = child.preferredDimension();
                height += ( childDimension.getHeight() + vSpace );
            }

            width += childWidth;
            height -= vSpace;
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
