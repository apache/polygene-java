package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import org.qi4j.composite.Constraint;

/**
 * TODO
 */
public final class ConstraintInstance
{
    private Constraint constraint;
    private Annotation annotation;

    public ConstraintInstance( Constraint constraint, Annotation annotation )
    {
        this.constraint = constraint;
        this.annotation = annotation;
    }

    public Constraint getConstraint()
    {
        return constraint;
    }

    public Annotation getAnnotation()
    {
        return annotation;
    }
}
