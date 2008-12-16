package org.qi4j.runtime.entity.association;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * TODO
 */
public class ManyAssociationInstance<T> extends AbstractAssociationInstance<T>
    implements ManyAssociation<T>
{
    private Collection<QualifiedIdentity> associated;

    public ManyAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork, Collection<QualifiedIdentity> associated )
    {
        super( associationInfo, unitOfWork );
        if( associated == null )
        {
            throw new IllegalArgumentException( "ManyAssociation must be a valid collection, shared with state" );
        }
        this.associated = associated;
    }

    public boolean removeAll( Collection<?> objects )
    {
        checkImmutable();
        return associated.removeAll( getEntityIdCollection( objects ) );
    }

    public boolean isEmpty()
    {
        return associated.isEmpty();
    }

    public boolean contains( Object o )
    {
        checkType( o );

        return associated.contains( getEntityId( o ) );
    }

    public Object[] toArray()
    {
        Object[] ids = associated.toArray();
        for( int i = 0; i < ids.length; i++ )
        {
            ids[ i ] = getEntity( (QualifiedIdentity) ids[ i ] );
        }

        return ids;
    }

    public <T> T[] toArray( T[] ts )
    {
        QualifiedIdentity[] ids = new QualifiedIdentity[ts.length];
        associated.toArray( ids );
        for( int i = 0; i < ids.length; i++ )
        {
            QualifiedIdentity id = ids[ i ];
            ts[ i ] = (T) getEntity( id );
        }
        return ts;
    }

    public boolean add( T t )
    {
        checkImmutable();
        checkType( t );

        return associated.add( getEntityId( t ) );
    }

    public boolean remove( Object o )
    {
        checkImmutable();
        checkType(o);

        return associated.remove( getEntityId( o ) );
    }

    public boolean containsAll( Collection<?> objects )
    {
        return associated.containsAll( getEntityIdCollection( objects ) );
    }

    public boolean addAll( Collection<? extends T> ts )
    {
        checkImmutable();
        return associated.addAll( getEntityIdCollection( ts ) );
    }

    public boolean retainAll( Collection<?> objects )
    {
        checkImmutable();
        return associated.retainAll( getEntityIdCollection( objects ) );
    }

    public void clear()
    {
        checkImmutable();
        associated.clear();
    }

    public String toString()
    {
        return associated.toString();
    }

    public Iterator<T> iterator()
    {
        return new ManyAssociationIterator( associated.iterator() );
    }

    public int size()
    {
        return associated.size();
    }

    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        if( !super.equals( o ) )
        {
            return false;
        }

        ManyAssociationInstance that = (ManyAssociationInstance) o;

        if( !associated.equals( that.associated ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + associated.hashCode();
        return result;
    }

    protected Collection<QualifiedIdentity> getEntityIdCollection( Collection ts )
    {
        ArrayList<QualifiedIdentity> list = new ArrayList<QualifiedIdentity>();
        for( Object t : ts )
        {
            list.add( getEntityId( t ) );
        }
        return list;
    }

    public void refresh( Collection<QualifiedIdentity> newAssociations )
    {
        if( newAssociations == null )
        {
            throw new IllegalArgumentException( "ManyAssociation must be a valid collection, shared with state" );
        }
        associated = newAssociations;
    }


    protected class ManyAssociationIterator
        implements Iterator<T>
    {
        private final Iterator<QualifiedIdentity> idIterator;

        public ManyAssociationIterator( Iterator<QualifiedIdentity> idIterator )
        {
            this.idIterator = idIterator;
        }

        public boolean hasNext()
        {
            return idIterator.hasNext();
        }

        public T next()
        {
            return getEntity( idIterator.next() );
        }

        public void remove()
        {
            checkImmutable();
            idIterator.remove();
        }
    }

    @Override protected void checkType( Object instance )
    {
        if( instance == null )
        {
            throw new NullPointerException( "Associated object may not be null" );
        }

        super.checkType( instance );
    }

    protected boolean isSet()
    {
        return true;
    }
}
