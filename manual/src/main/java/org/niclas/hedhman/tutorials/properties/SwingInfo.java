package org.niclas.hedhman.tutorials.properties;

import java.awt.Rectangle;
import java.util.Locale;
import javax.swing.Icon;

// START SNIPPET: info
public interface SwingInfo
{
    Icon icon( Rectangle size );

    String displayName( Locale locale );
}
// END SNIPPET: info
