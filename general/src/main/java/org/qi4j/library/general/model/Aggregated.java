package org.qi4j.library.general.model;

import java.io.Serializable;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.injection.scope.PropertyField;
import org.qi4j.property.Property;

/**
 * TODO
 */
@Mixins( Aggregated.AggregatedMixin.class )
public interface Aggregated
{
    Property<Composite> aggregate();

    Property<Composite> rootAggregate();

    final class AggregatedMixin
        implements Aggregated, Serializable
    {
        private static final long serialVersionUID = 1L;

        @PropertyField
        private Property<Composite> aggregate;

        public final Property<Composite> aggregate()
        {
            return aggregate;
        }

        public final Property<Composite> rootAggregate()
        {
            Property<Composite> current = aggregate();
            while( current instanceof Aggregated )
            {
                current = ( (Aggregated) current ).aggregate();
            }

            return current;
        }
    }
}
