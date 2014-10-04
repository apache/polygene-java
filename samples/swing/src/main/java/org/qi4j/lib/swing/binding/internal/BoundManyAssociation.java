package org.qi4j.lib.swing.binding.internal;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.lib.swing.binding.Binding;
import org.qi4j.lib.swing.binding.SwingAdapter;

public class BoundManyAssociation<T> extends AbstractBinding<T>
    implements Binding, ManyAssociation<T>
{

    private ManyAssociation<T> actualAssociations;

    public BoundManyAssociation( @Uses Method method, @Structure ObjectFactory objectBuilderFactory,
                                 @Service Iterable<SwingAdapter> allAdapters
    )
        throws IllegalArgumentException
    {
        super( method, objectBuilderFactory, allAdapters );
    }

    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return false;
    }

    public boolean add( T o )
    {
        return actualAssociations.add( o );
    }

    public boolean remove( T o )
    {
        return actualAssociations.remove( o );
    }

    public T get( int index )
    {
        return actualAssociations.get( index );
    }

    @Override
    public List<T> toList()
    {
        return actualAssociations.toList();
    }

    @Override
    public Set<T> toSet()
    {
        return actualAssociations.toSet();
    }

    @Override
    public int count()
    {
        return actualAssociations.count();
    }

    @Override
    public boolean contains( T entity )
    {
        return actualAssociations.contains( entity );
    }

    public boolean add( int index, T element )
    {
        return actualAssociations.add( index, element );
    }

    @Override
    public Iterator<T> iterator()
    {
        return actualAssociations.iterator();
    }

    @Override
    public Stream<T> stream()
    {
        return actualAssociations.stream();
    }
}
