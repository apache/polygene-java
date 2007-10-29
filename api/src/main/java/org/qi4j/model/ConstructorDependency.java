package org.qi4j.model;

import java.lang.reflect.Constructor;

/**
 * TODO
 */
public class ConstructorDependency
{
    private Constructor constructor;
    private Iterable<ParameterDependency> parameterDependencies;

    public ConstructorDependency( Constructor constructor, Iterable<ParameterDependency> parameterDependencies )
    {
        this.constructor = constructor;
        this.parameterDependencies = parameterDependencies;
    }

    public Constructor getConstructor()
    {
        return constructor;
    }

    public Iterable<ParameterDependency> getParameterDependencies()
    {
        return parameterDependencies;
    }
}