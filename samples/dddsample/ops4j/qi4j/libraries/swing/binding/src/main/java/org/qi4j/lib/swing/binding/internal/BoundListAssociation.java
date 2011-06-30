package org.qi4j.lib.swing.binding.internal;

import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.lib.swing.binding.Binding;
import org.qi4j.lib.swing.binding.SwingAdapter;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BoundListAssociation<T> extends AbstractBinding<T>
    implements Binding, ListAssociation<T>
{

    private ListAssociation<T> actualAssociations;

    public BoundListAssociation( @Uses Method method, @Structure ObjectBuilderFactory objectBuilderFactory,
                                 @Service Iterable<SwingAdapter> allAdapters )
        throws IllegalArgumentException
    {
        super( method, objectBuilderFactory, allAdapters );
    }

    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return false;
    }

    public int size()
    {
        return actualAssociations.size();
    }

    public boolean isEmpty()
    {
        return actualAssociations.isEmpty();
    }

    public boolean contains( Object o )
    {
        return actualAssociations.contains( o );
    }

    public Iterator<T> iterator()
    {
        return actualAssociations.iterator();
    }

    public Object[] toArray()
    {
        return actualAssociations.toArray();
    }

    public <T> T[] toArray( T[] a )
    {
        return actualAssociations.toArray( a );
    }

    public boolean add( T o )
    {
        return actualAssociations.add( o );
    }

    public boolean remove( Object o )
    {
        return actualAssociations.remove( o );
    }

    public boolean containsAll( Collection<?> c )
    {
        return actualAssociations.containsAll( c );
    }

    public boolean addAll( Collection<? extends T> c )
    {
        return actualAssociations.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends T> c )
    {
        return actualAssociations.addAll( index, c );
    }

    public boolean removeAll( Collection<?> c )
    {
        return actualAssociations.removeAll( c );
    }

    public boolean retainAll( Collection<?> c )
    {
        return actualAssociations.retainAll( c );
    }

    public void clear()
    {
        actualAssociations.clear();
    }

    public T get( int index )
    {
        return actualAssociations.get( index );
    }

    public T set( int index, T element )
    {
        return actualAssociations.set( index, element );
    }

    public void add( int index, T element )
    {
        actualAssociations.add( index, element );
    }

    public T remove( int index )
    {
        return actualAssociations.remove( index );
    }

    public int indexOf( Object o )
    {
        return actualAssociations.indexOf( o );
    }

    public int lastIndexOf( Object o )
    {
        return actualAssociations.lastIndexOf( o );
    }

    public ListIterator<T> listIterator()
    {
        return actualAssociations.listIterator();
    }

    public ListIterator<T> listIterator( int index )
    {
        return actualAssociations.listIterator( index );
    }

    public List<T> subList( int fromIndex, int toIndex )
    {
        return actualAssociations.subList( fromIndex, toIndex );
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return actualAssociations.metaInfo( infoType );
    }

    public String name()
    {
        return actualAssociations.name();
    }

    public String qualifiedName()
    {
        return actualAssociations.qualifiedName();
    }
}
