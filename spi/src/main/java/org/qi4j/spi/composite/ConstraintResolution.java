package org.qi4j.spi.composite;

import java.lang.annotation.Annotation;

/**
 * TODO
 */
public final class ConstraintResolution
{
    private ConstraintModel constraintModel;
    private Annotation constraintAnnotation;

    public ConstraintResolution( ConstraintModel constraintModel, Annotation constraintAnnotation )
    {
        this.constraintModel = constraintModel;
        this.constraintAnnotation = constraintAnnotation;
    }

    public ConstraintModel getConstraintModel()
    {
        return constraintModel;
    }

    public Annotation getConstraintAnnotation()
    {
        return constraintAnnotation;
    }
}
