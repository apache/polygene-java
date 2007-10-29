package org.qi4j.runtime.resolution;

import org.qi4j.model.MethodConstraint;

/**
 * TODO
 */
public class MethodConstraintResolution
{
    MethodConstraint constraint;
    Iterable<ParameterConstraintResolution> parameterConstraintResolutions;

    public MethodConstraintResolution( MethodConstraint constraint, Iterable<ParameterConstraintResolution> parameterConstraintResolutions )
    {
        this.constraint = constraint;
        this.parameterConstraintResolutions = parameterConstraintResolutions;
    }

    public MethodConstraint getConstraint()
    {
        return constraint;
    }

    public Iterable<ParameterConstraintResolution> getParameterConstraintResolutions()
    {
        return parameterConstraintResolutions;
    }
}
