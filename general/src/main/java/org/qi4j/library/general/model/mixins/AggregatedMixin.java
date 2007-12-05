package org.qi4j.library.general.model.mixins;

import org.qi4j.annotation.scope.PropertyField;
import org.qi4j.composite.Composite;
import org.qi4j.library.general.model.Aggregated;

/**
 * TODO
 */
public class AggregatedMixin
    implements Aggregated
{
    @PropertyField Composite aggregate;

    public Composite getAggregate()
    {
        return aggregate;
    }

    public Composite getRootAggregate()
    {
        Composite current = aggregate;
        while( current instanceof Aggregated )
        {
            current = ( (Aggregated) current ).getAggregate();
        }

        return current;
    }
}
