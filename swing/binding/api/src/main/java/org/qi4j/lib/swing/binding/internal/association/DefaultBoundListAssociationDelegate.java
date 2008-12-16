package org.qi4j.lib.swing.binding.internal.association;

import java.util.Map;
import javax.swing.JComponent;
import org.qi4j.api.entity.association.ListAssociation;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.lib.swing.binding.SwingAdapter;

/**
 * @author Lan Boon Ping
 */
public class DefaultBoundListAssociationDelegate<T> extends DefaultBoundManyAssociationDelegate<T, ListAssociation<T>>
{

    public DefaultBoundListAssociationDelegate( @Uses Map<Class<? extends JComponent>, SwingAdapter> adapters )
        throws IllegalArgumentException
    {
        super( adapters );
    }

    @Override
    protected void handleFromManyAssociationToSwing( SwingAdapter<ManyAssociation> adapter, JComponent component,
                                                     ListAssociation<T> manyAssociation )
    {
        adapter.fromDataToSwing( component, manyAssociation );
    }

    @Override
    protected void handleFromSwingToManyAssociation( SwingAdapter<ManyAssociation> adapter, JComponent component,
                                                     ListAssociation<T> manyAssociation )
    {
        adapter.fromSwingToData( component, manyAssociation );
    }
}
