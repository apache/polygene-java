package org.qi4j.library.general.model.modifiers;

import java.util.List;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.library.framework.validation.Validatable;
import org.qi4j.library.framework.validation.ValidationMessage;
import org.qi4j.library.general.model.Aggregated;

/**
 * Ensure that validation rules of aggregator are enforced.
 */
public abstract class AggregateValidationConcern
    implements Validatable
{
    @ThisCompositeAs Aggregated aggregated;
    @ConcernFor Validatable next;

    public List<ValidationMessage> validate()
    {
        List<ValidationMessage> messages = next.validate();

        Object aggregator = aggregated.getAggregate();
        if( aggregator instanceof Validatable )
        {
            Validatable aggregatorValidation = (Validatable) aggregator;
            messages.addAll( aggregatorValidation.validate() );

        }
        return messages;
    }
}
