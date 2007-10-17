package org.qi4j.api;

import java.lang.annotation.Annotation;

/**
 * When a constraint violation has occurred it is registered as a ConstraintViolation
 * and exposed through the InvocationContext for assertions and mixins to use.
 */
public class ConstraintViolation
{
    Annotation constraint;
    Object value;

    public ConstraintViolation( Annotation constraint, Object value )
    {
        this.constraint = constraint;
        this.value = value;
    }

    public Annotation getConstraint()
    {
        return constraint;
    }

    public Object getValue()
    {
        return value;
    }
}
