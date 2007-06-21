package org.qi4j.test.model1;

import org.qi4j.api.annotation.Modifies;

public final class Modifier7 implements Mixin3
{
    @Modifies Mixin3 next;

    public void setValue( String value )
    {
        next.setValue( value );
    }

    public String getValue()
    {
        return next.getValue();
    }
}
