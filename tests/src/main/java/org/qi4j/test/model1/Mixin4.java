package org.qi4j.test.model1;

import java.io.Serializable;

public interface Mixin4 extends Serializable
{
    void setValue( String value );

    String getValue();
}
