package org.qi4j.test.model1;

import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.library.framework.properties.PropertiesMixin;

@ImplementedBy( { PropertiesMixin.class } )
public interface Mixin3
{
    void setValue( String value );

    String getValue();
}
