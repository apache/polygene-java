package org.qi4j.tutorials.composites.tutorial4;

import org.qi4j.api.mixin.Mixins;

/**
 * This interface contains only the behaviour
 * of the HelloWorld object.
 * <p/>
 * It declares what Mixin to use as default implementation.
 */
@Mixins( HelloWorldBehaviourMixin.class )
public interface HelloWorldBehaviour
{
    String say();
}
