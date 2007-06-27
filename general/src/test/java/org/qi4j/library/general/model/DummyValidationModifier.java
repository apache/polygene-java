package org.qi4j.library.general.model;

import org.qi4j.api.annotation.Modifies;

public final class DummyValidationModifier implements Validatable
{
    public static boolean validateIsCalled = false;
    @Modifies private Validatable next;

    public void validate() throws ValidationException
    {
        validateIsCalled = true;
        next.validate();
    }
}
