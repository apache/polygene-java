package org.qi4j.lib.swing.binding.internal.association;

import java.util.Map;
import javax.swing.JComponent;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.injection.scope.Uses;
import org.qi4j.lib.swing.binding.SwingAdapter;

/**
 * @author Lan Boon Ping
 */
public class DefaultListAssociationDelegate<T> extends DefaultManyAssociationDelegate<T, ListAssociation<T>>
{

    public DefaultListAssociationDelegate( @Uses Map<Class<? extends JComponent>, SwingAdapter> adapters )
        throws IllegalArgumentException
    {
        super( adapters );
    }

    @Override
    protected void handleFromManyAssociationToSwing( SwingAdapter adapter, JComponent component,
                                                     ListAssociation<T> manyAssociation )
    {
        adapter.fromListAssociationToSwing( component, manyAssociation );
    }

    @Override
    protected void handleFromSwingToManyAssociation( SwingAdapter adapter, JComponent component,
                                                     ListAssociation<T> manyAssociation )
    {
        adapter.fromSwingToListAssociation( component, manyAssociation );
    }
}
