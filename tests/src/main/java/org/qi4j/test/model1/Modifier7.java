package org.qi4j.test.model1;

import org.qi4j.composite.scope.ConcernFor;

public final class Modifier7 implements Mixin3
{
    @ConcernFor Mixin3 next;

    public void setValue( String value )
    {
        next.setValue( value );
    }

    public String getValue()
    {
        return next.getValue();
    }
}
