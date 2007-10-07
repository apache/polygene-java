package org.qi4j.library.general.model.modifiers;

import java.util.List;
import org.qi4j.api.annotation.scope.AssertionFor;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.library.general.model.Aggregated;
import org.qi4j.library.general.model.Validatable;
import org.qi4j.library.general.model.ValidationException;
import org.qi4j.library.general.model.mixins.ValidationMessage;

/**
 * Ensure that validation rules of aggregator are enforced.
 */
public class AggregateValidationAssertion
    implements Validatable
{
    @ThisAs Aggregated aggregated;
    @AssertionFor Validatable next;

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


    public void checkValid() throws ValidationException
    {
        next.checkValid();
    }
}
