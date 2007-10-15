package org.qi4j.library.framework.validation.annotation.assertion;

import org.qi4j.api.annotation.Assertions;
import org.qi4j.library.framework.validation.ValidatableComposite;

/**
 * TODO
 */

@Assertions( { NotNullValidatableAssertion.class,
    MinLengthValidatableAssertion.class,
    MaxLengthValidatableAssertion.class,
    GreaterThanValidatableAssertion.class,
    LessThanValidatableAssertion.class,
    ContainsValidatableAssertion.class,
    MatchesValidatableAssertion.class } )
public interface DesignByContractComposite
    extends ValidatableComposite
{
}
