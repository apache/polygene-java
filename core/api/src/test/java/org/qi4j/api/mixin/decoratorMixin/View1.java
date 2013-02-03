package org.qi4j.api.mixin.decoratorMixin;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

// START SNIPPET: decorator
@Mixins(View1.Mixin.class)
public interface View1
{
    String bar();

    public class Mixin
        implements View1
    {
        @This
        FooModel model;

        @Override
        public String bar()
        {
            return model.getBar();
        }
    }
}
// END SNIPPET: decorator
