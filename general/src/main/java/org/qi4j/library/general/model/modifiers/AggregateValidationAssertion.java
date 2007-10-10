package org.qi4j.library.general.model.modifiers;

import java.util.List;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.library.general.model.Aggregated;
import org.qi4j.library.general.model.Validatable;
import org.qi4j.library.general.model.ValidationMessage;

/**
 * Ensure that validation rules of aggregator are enforced.
 */
public class AggregateValidationAssertion
    extends AbstractValidatableAssertion
{
    @ThisAs Aggregated aggregated;

    public List<ValidationMessage> isValid()
    {
        List<ValidationMessage> messages = next.isValid();

        Object aggregator = aggregated.getAggregate();
        if( aggregator instanceof Validatable )
        {
            Validatable aggregatorValidation = (Validatable) aggregator;
            messages.addAll( aggregatorValidation.isValid() );

        }
        return messages;
    }
}
