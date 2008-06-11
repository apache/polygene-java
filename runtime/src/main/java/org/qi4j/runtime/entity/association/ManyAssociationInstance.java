package org.qi4j.runtime.entity.association;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.association.AssociationInfo;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * TODO
 */
public class ManyAssociationInstance<T>
    extends AbstractAssociationInstance<T>
    implements ManyAssociation<T>
{
    private Collection<QualifiedIdentity> associated;

    public ManyAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork, Collection<QualifiedIdentity> associated )
    {
        super( associationInfo, unitOfWork );
        this.associated = associated;
    }

    public boolean removeAll( Collection<?> objects )
    {
        return associated.removeAll( getEntityIdCollection( objects ) );
    }

    public boolean isEmpty()
    {
        return associated.isEmpty();
    }

    public boolean contains( Object o )
    {
        if( !( o instanceof EntityComposite ) )
        {
            throw new IllegalArgumentException( "Object must be an EntityComposite" );
        }

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
        if( !( t instanceof EntityComposite ) )
        {
            throw new IllegalArgumentException( "Associated object must be an EntityComposite" );
        }

        return associated.add( getEntityId( t ) );
    }

    public boolean remove( Object o )
    {
        if( !( o instanceof EntityComposite ) )
        {
            throw new IllegalArgumentException( "Associated object must be an EntityComposite" );
        }

        return associated.remove( getEntityId( o ) );
    }

    public boolean containsAll( Collection<?> objects )
    {
        return associated.containsAll( getEntityIdCollection( objects ) );
    }

    public boolean addAll( Collection<? extends T> ts )
    {
        return associated.addAll( getEntityIdCollection( ts ) );
    }

    public boolean retainAll( Collection<?> objects )
    {
        return associated.retainAll( getEntityIdCollection( objects ) );
    }

    public void clear()
    {
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

    public void refresh( Collection<QualifiedIdentity> newSet )
    {
        associated = newSet;
    }


    protected class ManyAssociationIterator
        implements Iterator<T>
    {
        private Iterator<QualifiedIdentity> idIterator;

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
            idIterator.remove();
        }
    }

}
