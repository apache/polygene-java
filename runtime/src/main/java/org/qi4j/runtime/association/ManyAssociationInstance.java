package org.qi4j.runtime.association;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.functional.Function2;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.spi.entity.ManyAssociationState;

import java.lang.reflect.Type;
import java.util.*;

/**
 * JAVADOC
 */
public class ManyAssociationInstance<T>
    extends AbstractAssociationInstance<T>
    implements ManyAssociation<T>
{
    private ManyAssociationState manyAssociationState;

    public ManyAssociationInstance( AssociationInfo associationInfo,
                                    Function2<EntityReference, Type, Object> associationFunction,
                                    ManyAssociationState manyAssociationState
    )
    {
        super( associationInfo, associationFunction );
        this.manyAssociationState = manyAssociationState;
    }

    public int count()
    {
        return manyAssociationState.count();
    }

    public boolean contains( T entity )
    {
        return manyAssociationState.contains( getEntityReference( entity ) );
    }

    public boolean add( int i, T entity )
    {
        checkImmutable();
        checkType( entity );
        ((ConstraintsCheck) associationInfo).checkConstraints( entity );
        return manyAssociationState.add( i, getEntityReference( entity ) );
    }

    public boolean add( T entity )
    {
        return add( manyAssociationState.count(), entity );
    }

    public boolean remove( T entity )
    {
        checkImmutable();
        checkType( entity );

        return manyAssociationState.remove( getEntityReference( entity ) );
    }

    public T get( int i )
    {
        return getEntity( manyAssociationState.get( i ) );
    }

    public List<T> toList()
    {
        ArrayList<T> list = new ArrayList<T>();
        for( EntityReference entityReference : manyAssociationState )
        {
            list.add( getEntity( entityReference ) );
        }

        return list;
    }

    public Set<T> toSet()
    {
        Set<T> set = new HashSet<T>();
        for( EntityReference entityReference : manyAssociationState )
        {
            set.add( getEntity( entityReference ) );
        }

        return set;
    }

    public String toString()
    {
        return manyAssociationState.toString();
    }

    public Iterator<T> iterator()
    {
        return new ManyAssociationIterator( manyAssociationState.iterator() );
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

        return manyAssociationState.equals( that.manyAssociationState );
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + manyAssociationState.hashCode();
        return result;
    }

    public ManyAssociationState getManyAssociationState()
    {
        return manyAssociationState;
    }

    protected class ManyAssociationIterator
        implements Iterator<T>
    {
        private final Iterator<EntityReference> idIterator;

        public ManyAssociationIterator( Iterator<EntityReference> idIterator )
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

    @Override
    protected void checkType( Object instance )
    {
        if( instance == null )
        {
            throw new NullPointerException( "Associated object may not be null" );
        }

        super.checkType( instance );
    }
}
