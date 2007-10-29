package org.qi4j.runtime.resolution;

import java.util.List;
import org.qi4j.api.model.MethodModel;

/**
 * TODO
 */
public class MethodResolution
{
    MethodModel method;
    MethodConstraintResolution methodConstraintResolution;
    List<ConcernResolution> concerns;
    List<SideEffectResolution> sideEffects;
    MixinResolution mixin;

    public MethodResolution( MethodModel method, MethodConstraintResolution methodConstraintResolutions, List<ConcernResolution> concerns, List<SideEffectResolution> sideEffects, MixinResolution mixin )
    {
        this.method = method;
        this.methodConstraintResolution = methodConstraintResolutions;
        this.concerns = concerns;
        this.sideEffects = sideEffects;
        this.mixin = mixin;
    }

    public MethodModel getMethodModel()
    {
        return method;
    }

    public MethodConstraintResolution getMethodConstraintResolution()
    {
        return methodConstraintResolution;
    }

    public List<ConcernResolution> getConcerns()
    {
        return concerns;
    }

    public List<SideEffectResolution> getSideEffects()
    {
        return sideEffects;
    }

    public MixinResolution getMixinResolution()
    {
        return mixin;
    }

    @Override public String toString()
    {
        return method.getMethod().toGenericString();
    }
}
