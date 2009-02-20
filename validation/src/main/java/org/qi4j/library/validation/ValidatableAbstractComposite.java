package org.qi4j.library.validation;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;

/**
 * JAVADOC
 */
@Concerns( { ValidatableMessagesConcern.class, ChangeValidationConcern.class } )
@Mixins( { ValidatableMixin.class, UoWCallbackValidatableMixin.class, ValidationMessagesMixin.class } )
public interface ValidatableAbstractComposite
    extends Validatable, UnitOfWorkCallback
{
}
