package org.qi4j.runtime.composite;

/**
 * TODO
 */
public final class MethodDescriptor
{
    private CompositeMethodContext compositeMethodContext;
    private int invocationInstanceIndex;
    private int mixinIndex;
    private CompositeMethodInstancePool compositeMethodInstances;

    public MethodDescriptor( CompositeMethodContext compositeMethodBinding, int invocationInstanceIndex, int mixinIndex, CompositeMethodInstancePool compositeMethodInstances )
    {
        this.compositeMethodContext = compositeMethodBinding;
        this.invocationInstanceIndex = invocationInstanceIndex;
        this.mixinIndex = mixinIndex;
        this.compositeMethodInstances = compositeMethodInstances;
    }

    public CompositeMethodContext getCompositeMethodContext()
    {
        return compositeMethodContext;
    }

    public int getInvocationInstanceIndex()
    {
        return invocationInstanceIndex;
    }

    public int getMixinIndex()
    {
        return mixinIndex;
    }

    public CompositeMethodInstancePool getMethodInstances()
    {
        return compositeMethodInstances;
    }
}
