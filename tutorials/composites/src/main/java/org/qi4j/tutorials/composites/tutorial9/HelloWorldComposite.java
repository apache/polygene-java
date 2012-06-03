package org.qi4j.tutorials.composites.tutorial9;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

// START SNIPPET: solution

/**
 * This Composite interface declares transitively
 * all the Fragments of the HelloWorld composite.
 * <p/>
 * All standard declarations have been moved to
 * the StandardAbstractEntityComposite so we don't have to repeat
 * them in all Composites.
 */
@Mixins( { HelloWorldBehaviourMixin.class, GenericPropertyMixin.class } )
public interface HelloWorldComposite
    extends HelloWorldBehaviour, HelloWorldState, TransientComposite
{
}
// END SNIPPET: solution
