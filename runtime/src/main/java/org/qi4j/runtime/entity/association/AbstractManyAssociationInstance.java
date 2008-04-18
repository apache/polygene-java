package org.qi4j.runtime.entity.association;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.qi4j.entity.association.AssociationInfo;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;

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
