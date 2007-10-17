package org.qi4j.runtime.resolution;

import org.qi4j.api.model.ParameterConstraint;

/**
 * TODO
 */
public class ParameterConstraintResolution
{
    private ParameterConstraint parameterConstraint;
    private Iterable<ConstraintResolution> constraints;

    public ParameterConstraintResolution( ParameterConstraint parameterConstraint, Iterable<ConstraintResolution> constraints )
    {
        this.constraints = constraints;
        this.parameterConstraint = parameterConstraint;
    }

    public ParameterConstraint getParameterConstraint()
    {
        return parameterConstraint;
    }

    public Iterable<ConstraintResolution> getConstraints()
    {
        return constraints;
    }
}
