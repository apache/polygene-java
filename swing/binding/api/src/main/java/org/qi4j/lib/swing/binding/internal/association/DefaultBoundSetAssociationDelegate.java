package org.qi4j.lib.swing.binding.internal.association;

import java.util.Map;
import javax.swing.JComponent;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.injection.scope.Uses;
import org.qi4j.lib.swing.binding.SwingAdapter;

/**
 * @author Lan Boon Ping
 */
public class DefaultBoundSetAssociationDelegate<T> extends DefaultBoundManyAssociationDelegate<T, SetAssociation<T>>
{

    public DefaultBoundSetAssociationDelegate( @Uses Map<Class<? extends JComponent>, SwingAdapter> adapters )
        throws IllegalArgumentException
    {
        super( adapters );
    }

    protected void handleFromManyAssociationToSwing( SwingAdapter<ManyAssociation> adapter, JComponent component,
                                                     SetAssociation<T> manyAssociation )
    {
        adapter.fromDataToSwing( component, manyAssociation );
    }

    protected void handleFromSwingToManyAssociation( SwingAdapter<ManyAssociation> adapter, JComponent component,
                                                     SetAssociation<T> manyAssociation )
    {
        adapter.fromSwingToData( component, manyAssociation );
    }
}
