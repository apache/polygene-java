package org.qi4j.spi.composite;

/**
 * TODO
 */
public final class ConstraintsResolution
{
    private ConstraintsModel constraintsModel;
    private Iterable<ConstraintResolution> constraintResolutions;

    public ConstraintsResolution( ConstraintsModel constraintsModel, Iterable<ConstraintResolution> constraints )
    {
        this.constraintResolutions = constraints;
        this.constraintsModel = constraintsModel;
    }

    public ConstraintsModel getConstraintsModel()
    {
        return constraintsModel;
    }

    public Iterable<ConstraintResolution> getConstraintResolutions()
    {
        return constraintResolutions;
    }
}
