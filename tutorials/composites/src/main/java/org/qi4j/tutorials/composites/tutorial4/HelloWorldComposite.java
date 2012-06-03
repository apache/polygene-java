package org.qi4j.tutorials.composites.tutorial4;

import org.qi4j.api.composite.TransientComposite;

// START SNIPPET: solution

/**
 * This Composite interface declares all the Fragments
 * of the HelloWorld composite.
 * <p/>
 * The Mixins annotation has been moved to the respective sub-interfaces.
 * The sub-interfaces therefore declare themselves what mixin implementation
 * is preferred. This interface could still have its own Mixins annotation
 * with overrides of those defaults however.
 */
public interface HelloWorldComposite
    extends HelloWorld, TransientComposite
{
}
// END SNIPPET: solution
