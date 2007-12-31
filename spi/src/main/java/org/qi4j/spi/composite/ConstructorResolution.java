package org.qi4j.spi.composite;

/**
 * TODO
 */
public final class ConstructorResolution
{
    private ConstructorModel constructorModel;
    private Iterable<ParameterResolution> parameterResolutions;

    public ConstructorResolution( ConstructorModel constructorModel, Iterable<ParameterResolution> parameterResolutions )
    {
        this.constructorModel = constructorModel;
        this.parameterResolutions = parameterResolutions;
    }

    public ConstructorModel getConstructorModel()
    {
        return constructorModel;
    }

    public Iterable<ParameterResolution> getParameterResolutions()
    {
        return parameterResolutions;
    }
}
