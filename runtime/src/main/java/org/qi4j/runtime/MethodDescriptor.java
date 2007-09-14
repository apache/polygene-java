package org.qi4j.runtime;

import java.lang.reflect.Method;

/**
 * TODO
 */
public final class MethodDescriptor
{
    private Method method;
    private int invocationInstanceIndex;
    private int mixinIndex;
    private InvocationInstancePool invocationInstances;

    public MethodDescriptor( Method method, int invocationInstanceIndex, int mixinIndex, InvocationInstancePool invocationInstances )
    {
        this.method = method;
        this.invocationInstanceIndex = invocationInstanceIndex;
        this.mixinIndex = mixinIndex;
        this.invocationInstances = invocationInstances;
    }

    public Method getMethod()
    {
        return method;
    }

    public int getInvocationInstanceIndex()
    {
        return invocationInstanceIndex;
    }

    public int getMixinIndex()
    {
        return mixinIndex;
    }

    public InvocationInstancePool getInvocationInstances()
    {
        return invocationInstances;
    }
}
