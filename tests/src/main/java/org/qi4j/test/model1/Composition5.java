package org.qi4j.test.model1;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.test.model1.Modifier3;
import org.qi4j.test.model1.Mixin1Impl;
import org.qi4j.test.model1.Mixin2Impl;
import org.qi4j.test.model1.Mixin2;
import org.qi4j.test.model1.Mixin1;

@ModifiedBy( Modifier3.class)
@ImplementedBy({ Mixin1Impl.class, Mixin2Impl.class})
public interface Composition5 extends Mixin1, Mixin2, Composite
{
    
}
