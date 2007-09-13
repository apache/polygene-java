package org.qi4j.api.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.qi4j.api.DependencyKey;

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