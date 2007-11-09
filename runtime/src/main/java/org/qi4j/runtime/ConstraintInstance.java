package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import org.qi4j.ParameterConstraint;

/**
 * TODO
 */
public final class ConstraintInstance
{
    private ParameterConstraint constraint;
    private Annotation annotation;

    public ConstraintInstance( ParameterConstraint constraint, Annotation annotation )
    {
        this.constraint = constraint;
        this.annotation = annotation;
    }

    public ParameterConstraint getConstraint()
    {
        return constraint;
    }

    public Annotation getAnnotation()
    {
        return annotation;
    }
}
