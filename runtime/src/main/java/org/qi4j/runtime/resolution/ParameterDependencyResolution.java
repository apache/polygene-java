package org.qi4j.runtime.resolution;

import org.qi4j.model.ParameterDependency;
import org.qi4j.spi.dependency.DependencyResolution;

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