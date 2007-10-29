package org.qi4j.test.model1;

import org.qi4j.annotation.Mixins;
import org.qi4j.library.framework.properties.PropertiesMixin;

@Mixins( { PropertiesMixin.class } )
public interface Mixin3
{
    void setValue( String value );

    String getValue();
}
