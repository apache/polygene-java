package org.qi4j.tutorials.composites.tutorial3;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

// START SNIPPET: solution

/**
 * This Composite interface declares all the Fragments
 * of the HelloWorld composite.
 * <p/>
 * Currently it only declares one Mixin.
 */
@Mixins( HelloWorldMixin.class )
public interface HelloWorldComposite
    extends HelloWorld, TransientComposite
{
}
// END SNIPPET: solution
