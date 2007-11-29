package org.qi4j.spi.composite;

import java.lang.annotation.Annotation;

/**
 * TODO
 */
public class ConstraintResolution
{
    ConstraintModel constraintModel;
    Annotation constraintAnnotation;

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
