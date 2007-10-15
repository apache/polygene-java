package org.qi4j.library.framework.validation.annotation.assertion;

import org.qi4j.library.framework.validation.AbstractAnnotationValidatableAssertion;
import org.qi4j.library.framework.validation.annotation.GreaterThan;

/**
 * TODO
 */
public class GreaterThanValidatableAssertion
    extends AbstractAnnotationValidatableAssertion<GreaterThan, Number>
{
    protected boolean isValid( GreaterThan annotation, Number argument )
    {
        return argument.doubleValue() < annotation.value();
    }
}
