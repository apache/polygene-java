package org.qi4j.tutorials.composites.tutorial10;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

// START SNIPPET: solution

/**
 * This Composite interface declares transitively
 * all the Fragments of the HelloWorld composite.
 * <p/>
 * The Fragments are all abstract, so it's ok to
 * put the domain methods here. Otherwise the Fragments
 * would have to implement all methods, including those in Composite.
 */
@Mixins( { HelloWorldMixin.class } )
public interface HelloWorldComposite
    extends TransientComposite
{
    String say();
}
// END SNIPPET: solution
