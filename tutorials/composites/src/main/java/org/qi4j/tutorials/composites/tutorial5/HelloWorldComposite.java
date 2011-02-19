package org.qi4j.tutorials.composites.tutorial5;

import org.qi4j.api.composite.TransientComposite;

/**
 * This Composite interface declares transitively
 * all the Fragments of the HelloWorld composite.
 * <p/>
 * What Mixins to use and what Assertions should
 * apply to the methods can be found by exploring
 * the interfaces extended by this Composite interface,
 * and by looking at the declared Mixins.
 */
public interface HelloWorldComposite
    extends HelloWorld, TransientComposite
{
}
