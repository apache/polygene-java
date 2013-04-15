package org.qi4j.manual.recipes.properties;

import java.awt.Rectangle;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.qi4j.api.Qi4j;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;

public class SwingPanel
{
    private static final Rectangle SIZE_32_32 = new Rectangle( 32, 32 );
    private Locale locale;

// START SNIPPET: info-use
    @Structure
    private Qi4j api;
// END SNIPPET: info-use

// START SNIPPET: info-use
    private void addProperty( JPanel panel, Property<?> property )
    {
        SwingInfo info = api.propertyDescriptorFor( property ).metaInfo( SwingInfo.class );
        Icon icon = info.icon( SIZE_32_32 );
        panel.add(  new JLabel(info.displayName( this.locale ), icon, JLabel.CENTER) );
    }
// START SNIPPET: info-use
}
