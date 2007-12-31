package org.qi4j.spi.composite;

/**
 * TODO
 */
public final class ParameterConstraintsResolution
{
    private ParameterConstraintsModel parameterConstraintsModel;
    private Iterable<ConstraintResolution> constraintResolutions;

    public ParameterConstraintsResolution( ParameterConstraintsModel parameterConstraintsModel, Iterable<ConstraintResolution> constraints )
    {
        this.constraintResolutions = constraints;
        this.parameterConstraintsModel = parameterConstraintsModel;
    }

    public ParameterConstraintsModel getParameterConstraintModel()
    {
        return parameterConstraintsModel;
    }

    public Iterable<ConstraintResolution> getConstraintResolutions()
    {
        return constraintResolutions;
    }
}
