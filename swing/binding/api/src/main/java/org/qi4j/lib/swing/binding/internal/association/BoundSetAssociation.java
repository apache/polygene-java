package org.qi4j.lib.swing.binding.internal.association;

import java.lang.reflect.Method;
import org.qi4j.api.entity.association.SetAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.api.object.ObjectBuilderFactory;

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