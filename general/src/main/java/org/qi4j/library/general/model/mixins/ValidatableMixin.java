package org.qi4j.library.general.model.mixins;

import org.qi4j.library.general.model.Validatable;
import org.qi4j.library.general.model.ValidationException;

public class ValidatableMixin implements Validatable
{
    public void validate() throws ValidationException
    {
        //no operation, all work is done by Modifier.
    }
}
