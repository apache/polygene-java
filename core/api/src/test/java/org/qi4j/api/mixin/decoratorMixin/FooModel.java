package org.qi4j.api.mixin.decoratorMixin;

import org.qi4j.api.composite.DecoratorMixin;
import org.qi4j.api.mixin.Mixins;

// START SNIPPET: decorator
@Mixins(DecoratorMixin.class)
// START SNIPPET: plain
public interface FooModel
// END SNIPPET: decorator
{
    String getBar();
    void setBar(String value);
// END SNIPPET: plain

// START SNIPPET: plain
}
// END SNIPPET: plain
