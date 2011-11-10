package org.qi4j.runtime.value;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;

import java.util.Iterator;
import java.util.List;

/**
 * ManyAssociationState implementation for Value composites.
 */
public class ManyAssociationValueState
    implements ManyAssociationState
{
    private List<EntityReference> references;

    public ManyAssociationValueState( List<EntityReference> references )
    {
        this.references = references;
    }

    public int count()
    {
        return references.size();
    }

    public boolean contains( EntityReference entityReference )
    {
        return references.contains( entityReference );
    }

    public boolean add( int i, EntityReference entityReference )
    {
        if( references.contains( entityReference ) )
        {
            return false;
        }

        references.add( i, entityReference );
        return true;
    }

    public boolean remove( EntityReference entity )
    {
        boolean removed = references.remove( entity );
        return removed;
    }

    public EntityReference get( int i )
    {
        return references.get( i );
    }

    public Iterator<EntityReference> iterator()
    {
        final Iterator<EntityReference> iter = references.iterator();

        return new Iterator<EntityReference>()
        {
            EntityReference current;

            public boolean hasNext()
            {
                return iter.hasNext();
            }

            public EntityReference next()
            {
                current = iter.next();
                return current;
            }

            public void remove()
            {
                iter.remove();
            }
        };
    }
}
