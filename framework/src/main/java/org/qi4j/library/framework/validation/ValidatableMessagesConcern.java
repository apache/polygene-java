package org.qi4j.library.framework.validation;

import java.util.List;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.ConcernOf;
import org.qi4j.composite.scope.ThisCompositeAs;

/**
 * TODO
 */
@AppliesTo( Validatable.class )
public abstract class ValidatableMessagesConcern extends ConcernOf<Validatable>
    implements Validatable
{
    @ThisCompositeAs ValidationMessages messages;

    public List<ValidationMessage> validate()
    {
        List<ValidationMessage> messageList = next.validate();
        messageList.addAll( messages.getValidationMessages() );
        return messageList;
    }
}
