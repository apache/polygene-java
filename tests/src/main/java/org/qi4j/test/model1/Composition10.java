package org.qi4j.test.model1;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.library.framework.properties.PropertiesMixin;

@ImplementedBy( { PropertiesMixin.class } )
public interface Composition10 extends Mixin4, Composite
{
}
