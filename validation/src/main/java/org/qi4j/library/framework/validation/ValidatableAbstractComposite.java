package org.qi4j.library.framework.validation;

import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;

/**
 * TODO
 */
@Concerns( { ValidatableMessagesConcern.class, ChangeValidationConcern.class } )
@Mixins( { ValidatableMixin.class, ValidationMessagesMixin.class } )
public interface ValidatableAbstractComposite
    extends Validatable
{
}
