package org.qi4j.runtime.resolution;

import org.qi4j.dependency.DependencyResolution;
import org.qi4j.model.ParameterDependency;

/**
 * TODO
 */
public class ParameterDependencyResolution
{
    ParameterDependency parameter;
    DependencyResolution depedencyResolution;

    public ParameterDependencyResolution( ParameterDependency parameter, DependencyResolution depedencyResolution )
    {
        this.parameter = parameter;
        this.depedencyResolution = depedencyResolution;
    }


    public ParameterDependency getParameter()
    {
        return parameter;
    }

    public DependencyResolution getDependencyResolution()
    {
        return depedencyResolution;
    }
}