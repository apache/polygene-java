package org.qi4j.tutorials.composites.tutorial7;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffects;

// START SNIPPET: solution

/**
 * This Composite interface declares transitively
 * all the Fragments of the HelloWorld composite.
 * <p/>
 * It declares that the HelloWorldBehaviourSideEffect should be applied.
 */
@Mixins( { HelloWorldBehaviourMixin.class, HelloWorldStateMixin.class } )
@SideEffects( HelloWorldBehaviourSideEffect.class )
public interface HelloWorldComposite
    extends HelloWorld, TransientComposite
{
}
// END SNIPPET: solution
