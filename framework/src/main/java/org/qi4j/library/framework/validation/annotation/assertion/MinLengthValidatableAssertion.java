package org.qi4j.library.framework.validation.annotation.assertion;

import org.qi4j.library.framework.validation.AbstractAnnotationValidatableAssertion;
import org.qi4j.library.framework.validation.annotation.MinLength;

/**
 * TODO
 */
public class MinLengthValidatableAssertion
    extends AbstractAnnotationValidatableAssertion<MinLength, String>
{
    protected boolean isValid( MinLength annotation, String argument )
    {
        return argument.length() >= annotation.value();
    }
}
