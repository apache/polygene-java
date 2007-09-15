package org.qi4j.runtime.resolution;

import org.qi4j.api.model.MethodDependency;

/**
 * TODO
 */
public class MethodDependencyResolution
{
    private MethodDependency methodDependency;
    private Iterable<ParameterDependencyResolution> parameterDependencyResolutions;

    public MethodDependencyResolution( MethodDependency methodDependency, Iterable<ParameterDependencyResolution> parameterDependencyResolutions )
    {
        this.methodDependency = methodDependency;
        this.parameterDependencyResolutions = parameterDependencyResolutions;
    }

    public MethodDependency getMethodDependency()
    {
        return methodDependency;
    }

    public Iterable<ParameterDependencyResolution> getParameterDependencyResolutions()
    {
        return parameterDependencyResolutions;
    }
}