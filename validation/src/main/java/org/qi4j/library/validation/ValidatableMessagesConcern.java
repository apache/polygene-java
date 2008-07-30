package org.qi4j.library.validation;

import java.util.List;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.ConcernOf;
import org.qi4j.injection.scope.This;

/**
 * TODO
 */
@AppliesTo( Validatable.class )
public abstract class ValidatableMessagesConcern extends ConcernOf<Validatable>
    implements Validatable
{
    @This ValidationMessages messages;

    public List<ValidationMessage> validate()
    {
        List<ValidationMessage> messageList = next.validate();
        messageList.addAll( messages.getValidationMessages() );
        return messageList;
    }
}
