package org.qi4j.api.model;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class MethodConstraint
{
    private Method method;
    private Iterable<ParameterConstraint> parameterConstraints;

    public MethodConstraint( Method method, Iterable<ParameterConstraint> parameterConstraints )
    {
        this.method = method;
        this.parameterConstraints = parameterConstraints;
    }

    public Method getMethod()
    {
        return method;
    }

    public Iterable<ParameterConstraint> getParameterConstraints()
    {
        return parameterConstraints;
    }
}
