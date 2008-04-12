package org.qi4j.spi.composite;

/**
 * TODO
 */
public final class MethodConstraintResolution
{
    MethodConstraintModel constraintModel;
    Iterable<ConstraintsResolution> constraintResolutions;

    public MethodConstraintResolution( MethodConstraintModel constraintModel, Iterable<ConstraintsResolution> constraintResolutions )
    {
        this.constraintModel = constraintModel;
        this.constraintResolutions = constraintResolutions;
    }

    public MethodConstraintModel getConstraint()
    {
        return constraintModel;
    }

    public Iterable<ConstraintsResolution> getConstraintsResolutions()
    {
        return constraintResolutions;
    }
}
