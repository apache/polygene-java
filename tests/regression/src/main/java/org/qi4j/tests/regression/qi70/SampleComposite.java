package org.qi4j.tests.regression.qi70;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

@Mixins( SampleMixin.class )
public interface SampleComposite
    extends Sample, TransientComposite
{

}
