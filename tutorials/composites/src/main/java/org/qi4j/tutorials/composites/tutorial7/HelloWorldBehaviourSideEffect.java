package org.qi4j.tutorials.composites.tutorial7;

import org.qi4j.api.sideeffect.SideEffectOf;

// START SNIPPET: solution

/**
 * As a side-effect of calling say, output the result.
 */
public class HelloWorldBehaviourSideEffect
    extends SideEffectOf<HelloWorldBehaviour>
    implements HelloWorldBehaviour
{
    @Override
    public String say()
    {
        System.out.println( result.say() );
        return null;
    }
}
// END SNIPPET: solution
