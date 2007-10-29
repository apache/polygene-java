package org.qi4j.library.framework.validation;

import java.util.List;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.scope.ConcernFor;
import org.qi4j.api.annotation.scope.ThisAs;

/**
 * TODO
 */
@AppliesTo( Validatable.class )
public abstract class ValidatableMessagesConcern
    implements Validatable
{
    @ThisAs ValidationMessages messages;
    @ConcernFor Validatable next;

    public List<ValidationMessage> validate()
    {
        List<ValidationMessage> messageList = next.validate();
        messageList.addAll( messages.getValidationMessages() );
        return messageList;
    }
}
