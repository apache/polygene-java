package org.qi4j.lib.swing.binding.internal;

import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.property.Property;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

class PropertyFocusLostListener
    implements FocusListener
{

    private final JComponent component;
    private SwingAdapter adapter;
    private Property actual;

    public PropertyFocusLostListener( JComponent component )
    {
        this.component = component;
    }

    void use( SwingAdapter adapterToUse, Property actual )
    {
        adapter = adapterToUse;
        this.actual = actual;
    }

    public void focusGained( FocusEvent e )
    {
    }

    public void focusLost( FocusEvent e )
    {
        if( adapter != null )
        {
            adapter.fromSwingToProperty( component, actual );
        }
    }
}

