package org.qi4j.lib.swing.binding.internal.association;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import org.qi4j.api.entity.association.ListAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.api.object.ObjectBuilderFactory;

/**
 * @author Lan Boon Ping
 */
public class BoundListAssociation<T> extends BoundManyAssociation<T> implements ListAssociation<T>
{

    public BoundListAssociation( @Uses Method propertyMethod,
                                 @Structure ObjectBuilderFactory objectBuilderFactory,
                                 @Service Iterable<SwingAdapter> allAdapters )
    {
        super( propertyMethod, objectBuilderFactory, allAdapters );
    }

    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return capabilities.listAssociation;
    }

    public boolean addAll( int index, Collection<? extends T> c )
    {
        throw new IllegalArgumentException( "addAll() is not allowed in binding templates." );
    }

    public T get( int index )
    {
        throw new IllegalArgumentException( "get() is not allowed in binding templates." );
    }

    public T set( int index, T element )
    {
        throw new IllegalArgumentException( "set() is not allowed in binding templates." );
    }

    public void add( int index, T element )
    {
        throw new IllegalArgumentException( "add() is not allowed in binding templates." );
    }

    public T remove( int index )
    {
        throw new IllegalArgumentException( "remove() is not allowed in binding templates." );
    }

    public int indexOf( Object o )
    {
        throw new IllegalArgumentException( "indexOf() is not allowed in binding templates." );
    }

    public int lastIndexOf( Object o )
    {
        throw new IllegalArgumentException( "lastIndexOf() is not allowed in binding templates." );
    }

    public ListIterator<T> listIterator()
    {
        throw new IllegalArgumentException( "listIterator() is not allowed in binding templates." );
    }

    public ListIterator<T> listIterator( int index )
    {
        throw new IllegalArgumentException( "listIterator() is not allowed in binding templates." );
    }

    public List<T> subList( int fromIndex, int toIndex )
    {
        throw new IllegalArgumentException( "subList() is not allowed in binding templates." );
    }

}
