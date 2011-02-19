package org.qi4j.lib.swing.binding.internal;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComponent;
import org.qi4j.entity.association.Association;
import org.qi4j.lib.swing.binding.SwingAdapter;

public class AssociationFocusLostListener
    implements FocusListener
{

    private final JComponent component;
    private SwingAdapter adapter;
    private Association actual;

    public AssociationFocusLostListener( JComponent component )
    {
        this.component = component;
    }

    void use( SwingAdapter adapterToUse, Association actual )
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
            adapter.fromSwingToAssociation( component, actual );
        }
    }
}
