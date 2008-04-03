package org.qi4j.runtime.association;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.qi4j.association.AssociationInfo;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.serialization.EntityId;

/**
 * TODO
 */
public class AbstractManyAssociationInstance<T>
    extends AbstractAssociationInstance<T>
{
    public AbstractManyAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork )
    {
        super( associationInfo, unitOfWork );
    }

    protected Collection<EntityId> getEntityIdCollection( Collection ts )
    {
        ArrayList<EntityId> list = new ArrayList<EntityId>();
        for( Object t : ts )
        {
            list.add( getEntityId( t ) );
        }
        return list;
    }


    protected class ManyAssociationIterator
        implements Iterator<T>
    {
        private Iterator<EntityId> idIterator;

        public ManyAssociationIterator( Iterator<EntityId> idIterator )
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
