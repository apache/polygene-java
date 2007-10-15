package org.qi4j.library.framework.validation.annotation.assertion;

import org.qi4j.library.framework.validation.AbstractAnnotationValidatableAssertion;
import org.qi4j.library.framework.validation.annotation.NotNull;

/**
 * TODO
 */
public class NotNullValidatableAssertion
    extends AbstractAnnotationValidatableAssertion<NotNull, Object>
{
    protected boolean isValid( NotNull annotation, Object object )
    {
        return object != null;
    }
}
