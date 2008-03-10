package org.qi4j.test;

import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;

@Mixins( Qi4jTestMixin.class)
public interface Qi4jTestComposite
    extends Composite
{
}
