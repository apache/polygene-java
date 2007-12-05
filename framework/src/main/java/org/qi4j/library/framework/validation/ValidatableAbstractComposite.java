package org.qi4j.library.framework.validation;

import org.qi4j.annotation.Concerns;
import org.qi4j.annotation.Mixins;
import org.qi4j.composite.Composite;

/**
 * TODO
 */
@Concerns( { ValidatableMessagesConcern.class, ConstraintValidationConcern.class, ChangeValidationConcern.class } )
@Mixins( { ValidatableMixin.class, ValidationMessagesMixin.class } )
public interface ValidatableAbstractComposite
    extends Validatable, Composite
{
}
