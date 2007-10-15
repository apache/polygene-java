package org.qi4j.library.framework.validation.annotation.assertion;

import org.qi4j.library.framework.validation.AbstractAnnotationValidatableAssertion;
import org.qi4j.library.framework.validation.annotation.MaxLength;

/**
 * TODO
 */
public class MaxLengthValidatableAssertion
    extends AbstractAnnotationValidatableAssertion<MaxLength, String>
{
    protected boolean isValid( MaxLength annotation, String argument )
    {
        return argument.length() <= annotation.value();
    }
}
