package org.qi4j.runtime.resolution;

import java.util.List;
import org.qi4j.api.model.MethodModel;

/**
 * TODO
 */
public class MethodResolution
{
    MethodModel method;
    List<AssertionResolution> assertions;
    List<SideEffectResolution> sideEffects;
    MixinResolution mixin;

    public MethodResolution( MethodModel method, List<AssertionResolution> assertions, List<SideEffectResolution> sideEffects, MixinResolution mixin )
    {
        this.method = method;
        this.assertions = assertions;
        this.sideEffects = sideEffects;
        this.mixin = mixin;
    }

    public MethodModel getMethodModel()
    {
        return method;
    }

    public List<AssertionResolution> getAssertions()
    {
        return assertions;
    }

    public List<SideEffectResolution> getSideEffects()
    {
        return sideEffects;
    }

    public MixinResolution getMixin()
    {
        return mixin;
    }
}
