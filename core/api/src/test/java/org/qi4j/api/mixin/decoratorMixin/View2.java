package org.qi4j.api.mixin.decoratorMixin;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

@Mixins(View2.Mixin.class)
public interface View2
{
    String bar();
    public class Mixin
        implements View2
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
