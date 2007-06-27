package org.qi4j.library.general.model.modifiers;

import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.persistence.Lifecycle;
import org.qi4j.library.general.model.Validatable;

public class LifecycleValidationModifier
    implements Lifecycle
{
    @Uses Validatable validation;
    @Modifies Lifecycle next;

    public void create()
    {
        validation.validate();

        next.create();
    }

    public void delete()
    {

    }
}

