package org.qi4j.api;

import org.qi4j.api.model.ParameterDependency;

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

    public DependencyResolution getDepedencyResolution()
    {
        return depedencyResolution;
    }
}