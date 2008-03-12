package org.qi4j.test.mock;

import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;

@Mixins( { MockRecorderMixin.class } )
public interface MockComposite
    extends MockRecorder, Composite
{
}