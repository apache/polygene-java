package org.qi4j.spi.composite;

/**
 * TODO
 */
public final class MethodResolution
{
    private MethodModel methodModel;
    private Iterable<ParameterResolution> parameterResolutions;

    public MethodResolution( MethodModel methodModel, Iterable<ParameterResolution> parameterResolutions )
    {
        this.methodModel = methodModel;
        this.parameterResolutions = parameterResolutions;
    }

    public MethodModel getMethodModel()
    {
        return methodModel;
    }

    public Iterable<ParameterResolution> getParameterResolutions()
    {
        return parameterResolutions;
    }
}
