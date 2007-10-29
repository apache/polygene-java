package org.qi4j.model;

import java.lang.reflect.Constructor;

/**
 * TODO
 */
public class ConstructorConstraint
{
    private Constructor constructor;
    private Iterable<ParameterConstraint> parameterConstraints;

    public ConstructorConstraint( Constructor constructor, Iterable<ParameterConstraint> parameterConstraints )
    {
        this.parameterConstraints = parameterConstraints;
        this.constructor = constructor;
    }

    public Constructor getConstructor()
    {
        return constructor;
    }

    public Iterable<ParameterConstraint> getParameterConstraints()
    {
        return parameterConstraints;
    }
}
