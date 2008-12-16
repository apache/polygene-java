package org.qi4j.library.validation;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;

/**
 * TODO
 */
@Concerns( { ValidatableMessagesConcern.class, ChangeValidationConcern.class } )
@Mixins( { ValidatableMixin.class, ValidationMessagesMixin.class } )
public interface ValidatableAbstractComposite
    extends Validatable
{
}
