package org.qi4j.runtime.resolution;

import org.qi4j.api.model.ConstructorDependency;

/**
 * TODO
 */
public class ConstructorDependencyResolution
{
    private ConstructorDependency constructorDependency;
    private Iterable<ParameterDependencyResolution> parameterDependencyResolutions;

    public ConstructorDependencyResolution( ConstructorDependency constructorDependency, Iterable<ParameterDependencyResolution> parameterDependencyResolutions )
    {
        this.constructorDependency = constructorDependency;
        this.parameterDependencyResolutions = parameterDependencyResolutions;
    }

    public ConstructorDependency getConstructorDependency()
    {
        return constructorDependency;
    }

    public Iterable<ParameterDependencyResolution> getParameterDependencyResolutions()
    {
        return parameterDependencyResolutions;
    }
}
