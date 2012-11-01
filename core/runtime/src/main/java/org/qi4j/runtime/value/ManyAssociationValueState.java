package org.qi4j.runtime.value;

import java.util.Iterator;
import java.util.List;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;

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

    @Override
    public int count()
    {
        return references.size();
    }

    @Override
    public boolean contains( EntityReference entityReference )
    {
        return references.contains( entityReference );
    }

    @Override
    public boolean add( int i, EntityReference entityReference )
    {
        if( references.contains( entityReference ) )
        {
            return false;
        }

        references.add( i, entityReference );
        return true;
    }

    @Override
    public boolean remove( EntityReference entity )
    {
        boolean removed = references.remove( entity );
        return removed;
    }

    @Override
    public EntityReference get( int i )
    {
        return references.get( i );
    }

    @Override
    public Iterator<EntityReference> iterator()
    {
        final Iterator<EntityReference> iter = references.iterator();

        return new Iterator<EntityReference>()
        {
            EntityReference current;

            @Override
            public boolean hasNext()
            {
                return iter.hasNext();
            }

            @Override
            public EntityReference next()
            {
                current = iter.next();
                return current;
            }

            @Override
            public void remove()
            {
                iter.remove();
            }
        };
    }
}
