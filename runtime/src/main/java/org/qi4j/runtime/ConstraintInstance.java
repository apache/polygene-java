package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import org.qi4j.api.Constraint;

/**
 * TODO
 */
public class ConstraintInstance
{
    Constraint constraint;
    Annotation annotation;

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
