package org.qi4j.manual.recipes.concern;

import org.qi4j.api.concern.Concerns;

// START SNIPPET: annotationUse
// START SNIPPET: class
@Concerns( MyGenericConcern.class )
public interface AnyMixinType
{

// START SNIPPET: class
    @MyAnnotation
    void doSomething();

    void doSomethingElse();

// END SNIPPET: class
}
// END SNIPPET: class
// END SNIPPET: annotationUse
