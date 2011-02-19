package org.qi4j.tutorials.composites.tutorial6;

import org.qi4j.api.composite.TransientComposite;

/**
 * This Composite interface declares transitively
 * all the Fragments of the HelloWorld composite.
 * <p/>
 * What Mixins to use and what Concerns should
 * apply to the methods can be found by exploring
 * the interfaces extended by this Composite interface,
 * and by looking at the declared @Mixins annotations.
 */
public interface HelloWorldComposite
    extends HelloWorld, TransientComposite
{
}
