package org.qi4j.library.general.model;

import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.library.general.model.mixins.ValidatableMixin;

/**
 * This is a service used for validation. The actual validation is not done in the mixin but in the modifier.
 */
@ImplementedBy( ValidatableMixin.class )
public interface Validatable
{
    void validate() throws ValidationException;
}
