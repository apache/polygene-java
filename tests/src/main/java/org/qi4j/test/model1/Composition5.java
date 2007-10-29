package org.qi4j.test.model1;

import org.qi4j.Composite;
import org.qi4j.annotation.Concerns;
import org.qi4j.annotation.Mixins;

@Concerns( Modifier3.class )
@Mixins( { Mixin1Impl.class, Mixin2Impl.class } )
public interface Composition5 extends Mixin1, Mixin2, Composite
{

}
