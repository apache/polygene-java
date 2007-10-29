package org.qi4j.runtime.resolution;

import java.lang.annotation.Annotation;
import org.qi4j.model.ConstraintDeclarationModel;

/**
 * TODO
 */
public class ConstraintResolution
{
    ConstraintDeclarationModel constraintDeclarationModel;
    Annotation constraintAnnotation;

    public ConstraintResolution( ConstraintDeclarationModel constraintDeclarationModel, Annotation constraintAnnotation )
    {
        this.constraintDeclarationModel = constraintDeclarationModel;
        this.constraintAnnotation = constraintAnnotation;
    }

    public ConstraintDeclarationModel getConstraintDeclarationModel()
    {
        return constraintDeclarationModel;
    }

    public Annotation getConstraintAnnotation()
    {
        return constraintAnnotation;
    }
}
