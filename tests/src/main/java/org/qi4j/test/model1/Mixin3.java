package org.qi4j.test.model1;

import org.qi4j.api.annotation.Mixins;
import org.qi4j.library.framework.PropertiesMixin;

@Mixins( { PropertiesMixin.class } )
public interface Mixin3
{
    void setValue( String value );

    String getValue();
}
