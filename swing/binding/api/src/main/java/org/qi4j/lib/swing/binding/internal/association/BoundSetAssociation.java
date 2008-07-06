package org.qi4j.lib.swing.binding.internal.association;

import java.lang.reflect.Method;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.object.ObjectBuilderFactory;

/**
 * @author Lan Boon Ping
 */
public class BoundSetAssociation<T> extends BoundManyAssociation<T>
    implements SetAssociation<T>
{

    public BoundSetAssociation( @Uses Method propertyMethod, @Structure ObjectBuilderFactory objectBuilderFactory,
                                @Service Iterable<SwingAdapter> allAdapters )
    {
        super( propertyMethod, objectBuilderFactory, allAdapters );
    }

    @Override
    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return capabilities.setAssociation;
    }
}