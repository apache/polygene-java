/*
 * Copyright 2008 Sonny Gill. All Rights Reserved.
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
package org.qi4j.library.swing.visualizer;

import prefuse.visual.VisualItem;
import prefuse.Display;
import java.awt.geom.Rectangle2D;

/**
 * @author Sonny Gill
 */
public class GraphUtils
{

    public static boolean isComposite( VisualItem item )
    {
        return item.getInt( GraphConstants.FIELD_TYPE ) == ApplicationGraph.TYPE_COMPOSITE;
    }

    public static String getItemName( VisualItem item )
    {
        return item.getString( GraphConstants.FIELD_NAME );
    }

    public static String getCompositeName( Class type )
    {
        return type.getSimpleName();
    }

    public static boolean displaySizeFitsScaledBounds( Display display, Rectangle2D bounds )
    {
        double scale = display.getScale();
        return ( bounds.getWidth() * scale == display.getWidth() ) && ( bounds.getHeight() * scale == display.getHeight() );
    }

    public static boolean displaySizeContainsScaledBounds( Display display, Rectangle2D bounds )
    {
        double scale = display.getScale();
        return ( display.getWidth() > bounds.getWidth() * scale ) && ( display.getHeight() > bounds.getHeight() * scale );
    }
}

