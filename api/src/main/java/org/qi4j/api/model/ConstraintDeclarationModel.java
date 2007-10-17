package org.qi4j.api.model;

import org.qi4j.api.Constraint;

/**
 * A ConstraintDeclarationModel matches each use
 * of the @Constraints annotation. One model is created
 * for each class in the @Constraints annotation.
 */
public class ConstraintDeclarationModel<T>
{
    private Class<? extends Constraint> constraintType;
    private Class annotationType;
    private Class<T> parameterType;
    private Class declaredBy;

    public ConstraintDeclarationModel( Class<? extends Constraint> constraintType, Class annotationType, Class<T> parameterType, Class declaredBy )
    {
        this.declaredBy = declaredBy;
        this.constraintType = constraintType;
        this.annotationType = annotationType;
        this.parameterType = parameterType;
    }

    public Class<? extends Constraint> getConstraintType()
    {
        return constraintType;
    }

    public Class getAnnotationType()
    {
        return annotationType;
    }

    public Class<T> getParameterType()
    {
        return parameterType;
    }

    public Class getDeclaredBy()
    {
        return declaredBy;
    }


    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        ConstraintDeclarationModel that = (ConstraintDeclarationModel) o;

        if( !constraintType.equals( that.constraintType ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return constraintType.hashCode();
    }


    @Override public String toString()
    {
        return "@" + annotationType.getName() + " for " + parameterType.getName() + " implemented by " + constraintType.getName();
    }
}
