package org.qi4j.test.model;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;

@ModifiedBy(Modifier3.class)
@ImplementedBy({Mixin1Impl.class, Mixin2Impl.class})
public interface Composition5 extends Mixin1, Mixin2, Composite
{
    
}
