package org.qi4j.lib.swing.binding;

import javax.swing.JComponent;
import org.qi4j.api.property.Property;

/**
 * @author Lan Boon Ping
 */
public interface TableBinding<T> extends SwingBinding<T>
{

    TableBinding<T> to( JComponent component );

    void bindColumn( Property property, int columnIndex );
}
