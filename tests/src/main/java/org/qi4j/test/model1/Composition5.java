package org.qi4j.test.model1;

import org.qi4j.composite.Composite;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;

@Concerns( Modifier3.class )
@Mixins( { Mixin1Impl.class, Mixin2Impl.class } )
public interface Composition5 extends Mixin1, Mixin2, Composite
{

}
