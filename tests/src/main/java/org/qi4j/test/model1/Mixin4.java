package org.qi4j.test.model1;

import org.qi4j.api.annotation.instance.PerEntityInstance;

@PerEntityInstance
public interface Mixin4
{
    void setValue( String value );

    String getValue();
}
