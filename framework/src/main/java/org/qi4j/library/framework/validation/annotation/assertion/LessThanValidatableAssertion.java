package org.qi4j.library.framework.validation.annotation.assertion;

import org.qi4j.library.framework.validation.AbstractAnnotationValidatableAssertion;
import org.qi4j.library.framework.validation.annotation.LessThan;

/**
 * TODO
 */
public class LessThanValidatableAssertion
    extends AbstractAnnotationValidatableAssertion<LessThan, Number>
{
    protected boolean isValid( LessThan annotation, Number argument )
    {
        return argument.doubleValue() < annotation.value();
    }
}
