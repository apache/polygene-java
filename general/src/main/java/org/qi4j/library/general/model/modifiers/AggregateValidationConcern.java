package org.qi4j.library.general.model.modifiers;

import java.util.List;
import org.qi4j.composite.ConcernOf;
import org.qi4j.injection.scope.This;
import org.qi4j.library.validation.Validatable;
import org.qi4j.library.validation.ValidationMessage;
import org.qi4j.library.general.model.Aggregated;

/**
 * Ensure that validation rules of aggregator are enforced.
 */
public abstract class AggregateValidationConcern extends ConcernOf<Validatable>
    implements Validatable
{
    @This Aggregated aggregated;

    public List<ValidationMessage> validate()
    {
        List<ValidationMessage> messages = next.validate();

        Object aggregator = aggregated.aggregate().get();
        if( aggregator instanceof Validatable )
        {
            Validatable aggregatorValidation = (Validatable) aggregator;
            messages.addAll( aggregatorValidation.validate() );

        }
        return messages;
    }
}
