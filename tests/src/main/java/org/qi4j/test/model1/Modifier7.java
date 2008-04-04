package org.qi4j.test.model1;

import org.qi4j.composite.ConcernOf;

public final class Modifier7 extends ConcernOf<Mixin3>
    implements Mixin3
{
    public void setValue( String value )
    {
        next.setValue( value );
    }

    public String getValue()
    {
        return next.getValue();
    }
}
