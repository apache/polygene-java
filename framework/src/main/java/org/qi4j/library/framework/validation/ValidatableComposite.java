package org.qi4j.library.framework.validation;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.Assertions;
import org.qi4j.api.annotation.Mixins;

/**
 * TODO
 */
@Assertions( { ValidatableMessagesAssertion.class, ChangeValidationAssertion.class } )
@Mixins( { ValidatableMixin.class, ValidationMessagesMixin.class } )
public interface ValidatableComposite
    extends Validatable, Composite
{
}
