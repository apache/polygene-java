package org.qi4j.spi.composite;

import org.qi4j.spi.injection.InjectionResolution;

/**
 * TODO
 */
public final class ParameterResolution
{
    private ParameterModel parameterModel;
    private ConstraintsResolution constraintsResolution;
    private InjectionResolution injectionResolution;

    public ParameterResolution( ParameterModel parameterModel, ConstraintsResolution constraintsResolutions, InjectionResolution injectionResolution )
    {
        this.parameterModel = parameterModel;
        this.constraintsResolution = constraintsResolutions;
        this.injectionResolution = injectionResolution;
    }

    public ParameterModel getParameterModel()
    {
        return parameterModel;
    }

    public ConstraintsResolution getConstraintsResolution()
    {
        return constraintsResolution;
    }

    public InjectionResolution getInjectionResolution()
    {
        return injectionResolution;
    }
}