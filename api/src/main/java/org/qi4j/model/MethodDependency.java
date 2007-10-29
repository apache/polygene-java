package org.qi4j.model;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class MethodDependency
{
    private Method method;
    private Iterable<ParameterDependency> parameterDependencies;

    public MethodDependency( Method method, Iterable<ParameterDependency> parameterDependencies )
    {
        this.method = method;
        this.parameterDependencies = parameterDependencies;
    }

    public Method getMethod()
    {
        return method;
    }

    public Iterable<ParameterDependency> getParameterDependencies()
    {
        return parameterDependencies;
    }
}