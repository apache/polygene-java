package org.qi4j.spi.composite;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * TODO
 */
public class ConstraintDeclarationModel
{
    private Class<? extends Annotation> annotationType;
    private List<ConstraintModel<?>> constraintModels;

    public ConstraintDeclarationModel( Class<? extends Annotation> annotationType, List<ConstraintModel<?>> constraintModels )
    {
        this.annotationType = annotationType;
        this.constraintModels = constraintModels;
    }

    public Class<? extends Annotation> getAnnotationType()
    {
        return annotationType;
    }

    public List<ConstraintModel<?>> getConstraintModels()
    {
        return constraintModels;
    }
}
