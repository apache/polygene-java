package org.qi4j.manual.recipes.sideeffects;

import org.qi4j.api.concern.Concerns;

// START SNIPPET: body
// START SNIPPET: annotation
@Concerns( MyGenericSideEffect.class )
public interface AnyMixinType
{
// END SNIPPET: body

    @MyAnnotation
    void doSomething();

    void doSomethingElse();

// START SNIPPET: body
}
// START SNIPPET: annotation
// END SNIPPET: body
