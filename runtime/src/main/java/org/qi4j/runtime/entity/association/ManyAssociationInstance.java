package org.qi4j.runtime.entity.association;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.unitofwork.ManyAssociationStateChange;
import org.qi4j.api.unitofwork.StateChangeListener;
import org.qi4j.api.unitofwork.StateChangeVoter;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * TODO
 */
public class ManyAssociationInstance<T> extends AbstractAssociationInstance<T>
    implements ManyAssociation<T>
{
    public ManyAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork, EntityState entityState )
    {
        super( associationInfo, unitOfWork, entityState );
    }

    public boolean removeAll( Collection<?> objects )
    {
        checkImmutable();

        // Allow voters to vote on change
        ManyAssociationStateChange change = vote( ManyAssociationStateChange.ChangeType.REMOVE, (Collection<Entity>) objects );

        try
        {
            return associated().removeAll( getEntityIdCollection( objects ) );
        }
        finally
        {
            notify( change, ManyAssociationStateChange.ChangeType.REMOVE, (Collection<Entity>) objects );
        }
    }

    public boolean isEmpty()
    {
        return associated().isEmpty();
    }

    public boolean contains( Object o )
    {
        checkType( o );

        return associated().contains( getEntityId( o ) );
    }

    public Object[] toArray()
    {
        Object[] ids = associated().toArray();
        for( int i = 0; i < ids.length; i++ )
        {
            ids[ i ] = getEntity( (QualifiedIdentity) ids[ i ] );
        }

        return ids;
    }

    public <T> T[] toArray( T[] ts )
    {
        QualifiedIdentity[] ids = new QualifiedIdentity[ts.length];
        associated().toArray( ids );
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

        // Allow voters to vote on change
        ManyAssociationStateChange change = vote( ManyAssociationStateChange.ChangeType.ADD, (Entity) t );

        try
        {
            return associated().add( getEntityId( t ) );
        }
        finally
        {
            notify( change, ManyAssociationStateChange.ChangeType.ADD, (Entity) t);
        }
    }

    public boolean remove( Object o )
    {
        checkImmutable();
        checkType(o);

        // Allow voters to vote on change
        ManyAssociationStateChange change = vote( ManyAssociationStateChange.ChangeType.REMOVE, (Entity) o );

        try
        {
            return associated().remove( getEntityId( o ) );
        }
        finally
        {
            notify( change, ManyAssociationStateChange.ChangeType.REMOVE, (Entity) o);
        }
    }

    public boolean containsAll( Collection<?> objects )
    {
        return associated().containsAll( getEntityIdCollection( objects ) );
    }

    public boolean addAll( Collection<? extends T> ts )
    {
        checkImmutable();

        // Allow voters to vote on change
        ManyAssociationStateChange change = vote( ManyAssociationStateChange.ChangeType.ADD, (Collection<Entity>) ts );

        try
        {
            return associated().addAll( getEntityIdCollection( ts ) );
        }
        finally
        {
            notify( change, ManyAssociationStateChange.ChangeType.ADD, (Collection<Entity>) ts);
        }
    }

    public boolean retainAll( Collection<?> objects )
    {
        checkImmutable();
        return associated().retainAll( getEntityIdCollection( objects ) );
    }

    public void clear()
    {
        checkImmutable();

        // Allow voters to vote on change
        ManyAssociationStateChange change = vote( ManyAssociationStateChange.ChangeType.CLEAR, Collections.<Entity>emptyList() );

        try
        {
            associated().clear();
        }
        finally
        {
            notify( change, ManyAssociationStateChange.ChangeType.CLEAR, Collections.<Entity>emptyList());
        }
    }

    public String toString()
    {
        return associated().toString();
    }

    public Iterator<T> iterator()
    {
        return new ManyAssociationIterator( associated().iterator() );
    }

    public int size()
    {
        return associated().size();
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

        if( !associated().equals( that.associated() ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + associated().hashCode();
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

    protected ManyAssociationStateChange vote( ManyAssociationStateChange.ChangeType changeType, Collection<Entity> changes)
    {
        ManyAssociationStateChange change = null;
        Iterable<StateChangeVoter> stateChangeVoters = unitOfWork.stateChangeVoters();
        if( stateChangeVoters != null )
        {
            change = new ManyAssociationStateChange( entityState.qualifiedIdentity().identity(), qualifiedName(), changeType, changes);

            for( StateChangeVoter stateChangeVoter : stateChangeVoters )
            {
                stateChangeVoter.acceptChange( change );
            }
        }
        return change;
    }

    protected ManyAssociationStateChange vote( ManyAssociationStateChange.ChangeType changeType, Entity changeEntity)
    {
        ManyAssociationStateChange change = null;
        Iterable<StateChangeVoter> stateChangeVoters = unitOfWork.stateChangeVoters();
        if( stateChangeVoters != null )
        {
            Collection<Entity> changes = Collections.singletonList( changeEntity );
            change = new ManyAssociationStateChange( entityState.qualifiedIdentity().identity(), qualifiedName(), changeType, changes);

            for( StateChangeVoter stateChangeVoter : stateChangeVoters )
            {
                stateChangeVoter.acceptChange( change );
            }
        }
        return change;
    }

    protected void notify( ManyAssociationStateChange change, ManyAssociationStateChange.ChangeType changeType, Collection<Entity> changes )
    {
        // Notify listeners
        Iterable<StateChangeListener> stateChangeListeners = unitOfWork.stateChangeListeners();
        if( stateChangeListeners != null )
        {
            if (change == null)
                change = new ManyAssociationStateChange( entityState.qualifiedIdentity().identity(), qualifiedName(), changeType, changes);

            for( StateChangeListener stateChangeListener : stateChangeListeners )
            {
                stateChangeListener.notify( change );
            }
        }
    }

    protected void notify( ManyAssociationStateChange change, ManyAssociationStateChange.ChangeType changeType, Entity changeEntity )
    {
        // Notify listeners
        Iterable<StateChangeListener> stateChangeListeners = unitOfWork.stateChangeListeners();
        if( stateChangeListeners != null )
        {
            if (change == null)
            {
                Collection<Entity> changes = Collections.singletonList( changeEntity );
                change = new ManyAssociationStateChange( entityState.qualifiedIdentity().identity(), qualifiedName(), changeType, changes);
            }

            for( StateChangeListener stateChangeListener : stateChangeListeners )
            {
                stateChangeListener.notify( change );
            }
        }
    }

    private Collection<QualifiedIdentity> associated()
    {
        return entityState.getManyAssociation( associationInfo.qualifiedName() );
    }
}
