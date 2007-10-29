package org.qi4j.library.framework.validation;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.Concerns;
import org.qi4j.api.annotation.Mixins;

/**
 * TODO
 */
@Concerns( { ValidatableMessagesConcern.class, ConstraintValidationConcern.class, ChangeValidationConcern.class } )
@Mixins( { ValidatableMixin.class, ValidationMessagesMixin.class } )
public interface ValidatableAbstractComposite
    extends Validatable, Composite
{
}
