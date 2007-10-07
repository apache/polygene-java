package org.qi4j.test.model1;

import org.qi4j.api.annotation.scope.AssertionFor;

public final class Modifier7 implements Mixin3
{
    @AssertionFor Mixin3 next;

    public void setValue( String value )
    {
        next.setValue( value );
    }

    public String getValue()
    {
        return next.getValue();
    }
}
