package org.qi4j.spi.composite;

/**
 * TODO
 */
public class MethodConstraintResolution
{
    MethodConstraintModel constraintModel;
    Iterable<ParameterConstraintsResolution> parameterConstraintResolutions;

    public MethodConstraintResolution( MethodConstraintModel constraintModel, Iterable<ParameterConstraintsResolution> parameterConstraintResolutions )
    {
        this.constraintModel = constraintModel;
        this.parameterConstraintResolutions = parameterConstraintResolutions;
    }

    public MethodConstraintModel getConstraint()
    {
        return constraintModel;
    }

    public Iterable<ParameterConstraintsResolution> getParameterConstraintResolutions()
    {
        return parameterConstraintResolutions;
    }
}
