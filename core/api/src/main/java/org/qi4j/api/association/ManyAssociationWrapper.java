package org.qi4j.api.association;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * If you want to catch calls to ManyAssociations, then create a GenericConcern
 * that wraps the Qi4j-supplied ManyAssociation instance with ManyAssociationWrappers. Override
 * methods to perform your custom code.
 */
public class ManyAssociationWrapper
    implements ManyAssociation<Object>
{
    protected ManyAssociation<Object> next;

    public ManyAssociationWrapper( ManyAssociation<Object> next )
    {
        this.next = next;
    }

    public ManyAssociation<Object> next()
    {
        return next;
    }

    @Override
    public int count()
    {
        return next.count();
    }

    @Override
    public boolean contains( Object entity )
    {
        return next.contains( entity );
    }

    @Override
    public boolean add( int i, Object entity )
    {
        return next.add( i, entity );
    }

    @Override
    public boolean add( Object entity )
    {
        return next.add( entity );
    }

    @Override
    public boolean remove( Object entity )
    {
        return next.remove( entity );
    }

    @Override
    public Object get( int i )
    {
        return next.get( i );
    }

    @Override
    public List<Object> toList()
    {
        return next.toList();
    }

    @Override
    public Set<Object> toSet()
    {
        return next.toSet();
    }

    @Override
    public Iterator<Object> iterator()
    {
        return next.iterator();
    }

    @Override
    public int hashCode()
    {
        return next.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return next.equals( obj );
    }

    @Override
    public String toString()
    {
        return next.toString();
    }
}
