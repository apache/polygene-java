/*
 * Copyright 2008 Edward Yakop. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.swing.visualizer.overview.internal.visualization.layout;

import java.awt.Dimension;
import java.awt.Font;
import static java.awt.Font.PLAIN;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import org.qi4j.composite.NullArgumentException;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import prefuse.render.Renderer;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
abstract class AbstractLayout
{
    protected static final Comparator<AbstractLayout> LABEL_COMPARATOR = new Comparator<AbstractLayout>()
    {
        public final int compare( AbstractLayout o1, AbstractLayout o2 )
        {
            String label1 = labelString( o1.nodeItem );
            String label2 = labelString( o2.nodeItem );
            return label1.compareTo( label2 );
        }
    };

    protected static final Font DEFAULT_LABEL_FONT = new Font( "Arial", PLAIN, 12 );

    /**
     * Node item of this layout. This might be null.
     */
    protected final NodeItem nodeItem;

    protected AbstractLayout( NodeItem aNodeItem )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( "aNodeItem", aNodeItem );
        nodeItem = aNodeItem;
    }

    protected AbstractLayout()
    {
        nodeItem = null;
    }

    /**
     * Apply layout to node.
     *
     * @param constraint Layout constraint.
     * @return Bounds of the node.
     * @since 0.5
     */
    public abstract Rectangle2D applyLayout( LayoutConstraint constraint );

    /**
     * @return preferred dimension.
     */
    public abstract Dimension preferredDimension();

    protected Dimension labelDimension()
    {
        String labelString = labelString( nodeItem );
        if( labelString != null )
        {
            Font font = labelFont();
            nodeItem.setFont( font );

            FontMetrics fm = Renderer.DEFAULT_GRAPHICS.getFontMetrics( font );
            int width = fm.stringWidth( labelString );
            int height = fm.getHeight();

            return new Dimension( width, height );
        }

        return null;
    }

    protected Font labelFont()
    {
        return DEFAULT_LABEL_FONT;
    }

    protected static String labelString( NodeItem node )
    {
        if( node != null )
        {
            return node.getString( FIELD_NAME );
        }

        return null;
    }
}