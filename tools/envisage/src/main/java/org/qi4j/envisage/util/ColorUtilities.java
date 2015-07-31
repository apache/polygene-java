/*
 * Copyright (c) 2009, Tony Kohar. All Rights Reserved.
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
package org.qi4j.envisage.util;

import java.awt.Color;

/**
 * Collection of Color Utilities
 */
public class ColorUtilities
{

    /**
     * Return the equivalent AWT {@code Color} of the supplied hexString in format eg #FF33D2.
     *
     * @param hexString Color in hexString format eg: #FF33D2
     * @return the AWT {@code Color} or null if fail to parse
     */
    public static Color hexStringToColor( String hexString )
    {
        Color color = null;
        int r, g, b;
        try
        {
            String tmpStr = hexString.substring( 1, 3 );
            r = Integer.parseInt( tmpStr, 16 );
            tmpStr = hexString.substring( 3, 5 );
            g = Integer.parseInt( tmpStr, 16 );
            tmpStr = hexString.substring( 5, 7 );
            b = Integer.parseInt( tmpStr, 16 );
            color = new Color( r, g, b );
        }
        catch( NumberFormatException | IndexOutOfBoundsException e )
        {
            e.printStackTrace();
        }
        return color;
    }

    private ColorUtilities()
    {
    }
}
