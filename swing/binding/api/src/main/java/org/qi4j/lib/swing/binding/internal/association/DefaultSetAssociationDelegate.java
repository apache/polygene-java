package org.qi4j.lib.swing.binding.internal.association;

import java.util.Map;
import javax.swing.JComponent;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.injection.scope.Uses;
import org.qi4j.lib.swing.binding.SwingAdapter;

/**
 * @author Lan Boon Ping
 */
public class DefaultSetAssociationDelegate<T> extends DefaultManyAssociationDelegate<T, SetAssociation<T>>
{

    public DefaultSetAssociationDelegate( @Uses Map<Class<? extends JComponent>, SwingAdapter> adapters )
        throws IllegalArgumentException
    {
        super( adapters );
    }

    protected void handleFromManyAssociationToSwing( SwingAdapter adapter, JComponent component,
                                                     SetAssociation<T> manyAssociation )
    {
        adapter.fromSetAssociationToSwing( component, manyAssociation );
    }

    protected void handleFromSwingToManyAssociation( SwingAdapter adapter, JComponent component,
                                                     SetAssociation<T> manyAssociation )
    {
        adapter.fromSwingToSetAssociation( component, manyAssociation );
    }
}
