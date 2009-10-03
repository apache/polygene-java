package org.qi4j.runtime.entity.association;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * JAVADOC
 */
public class ManyAssociationInstance<T>
    extends AbstractAssociationInstance<T>
    implements ManyAssociation<T>
{
    private ManyAssociationModel model;

    public ManyAssociationInstance( AssociationInfo associationInfo,
                                    ManyAssociationModel constraints,
                                    ModuleUnitOfWork unitOfWork,
                                    EntityState entityState
    )
    {
        super( associationInfo, unitOfWork, entityState );
        this.model = constraints;
    }

    public int count()
    {
        return associated().count();
    }

    public boolean contains( T entity )
    {
        return associated().contains( getEntityReference( entity ) );
    }

    public boolean add( int i, T entity )
    {
        checkImmutable();
        checkType( entity );
        model.checkConstraints( entity );
        try
        {
            return associated().add( i, getEntityReference( entity ) );
        }
        finally
        {
            model.checkAssociationConstraints( this );
        }
    }

    public boolean add( T entity )
    {
        return add( associated().count(), entity );
    }

    public boolean remove( T entity )
    {
        checkImmutable();
        checkType( entity );

        try
        {
            return associated().remove( getEntityReference( entity ) );
        }
        finally
        {
            model.checkAssociationConstraints( this );
        }
    }

    public T get( int i )
    {
        return getEntity( associated().get( i ) );
    }

    public List<T> toList()
    {
        ArrayList<T> list = new ArrayList<T>();
        for( EntityReference entityReference : associated() )
        {
            list.add( getEntity( entityReference ) );
        }

        return list;
    }

    public Set<T> toSet()
    {
        Set<T> set = new HashSet<T>();
        for( EntityReference entityReference : associated() )
        {
            set.add( getEntity( entityReference ) );
        }

        return set;
    }

    public String toString()
    {
        return associated().toString();
    }

    public Iterator<T> iterator()
    {
        return new ManyAssociationIterator( associated().iterator() );
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

        return associated().equals( that.associated() );
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

    protected boolean isSet()
    {
        return true;
    }

    private ManyAssociationState associated()
    {
        return entityState.getManyAssociation( ( model ).manyAssociationType().qualifiedName() );
    }
}
