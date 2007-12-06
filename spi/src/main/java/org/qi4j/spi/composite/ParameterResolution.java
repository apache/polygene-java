package org.qi4j.spi.composite;

import org.qi4j.spi.injection.InjectionResolution;

/**
 * TODO
 */
public class ParameterResolution
{
    private ParameterModel parameterModel;
    private ParameterConstraintsResolution parameterConstraintsResolution;
    private InjectionResolution injectionResolution;

    public ParameterResolution( ParameterModel parameterModel, ParameterConstraintsResolution parameterConstraintsResolutions, InjectionResolution injectionResolution )
    {
        this.parameterModel = parameterModel;
        this.parameterConstraintsResolution = parameterConstraintsResolutions;
        this.injectionResolution = injectionResolution;
    }

    public ParameterModel getParameterModel()
    {
        return parameterModel;
    }

    public ParameterConstraintsResolution getParameterConstraintResolution()
    {
        return parameterConstraintsResolution;
    }

    public InjectionResolution getInjectionResolution()
    {
        return injectionResolution;
    }
}