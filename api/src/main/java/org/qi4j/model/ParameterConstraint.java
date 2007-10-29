package org.qi4j.model;

import java.lang.annotation.Annotation;

/**
 * TODO
 */
public class ParameterConstraint
{
    private Class parameterType;
    private Iterable<Annotation> constraints;

    public ParameterConstraint( Class parameterType, Iterable<Annotation> constraints )
    {
        this.parameterType = parameterType;
        this.constraints = constraints;
    }

    public Class getParameterType()
    {
        return parameterType;
    }

    public Iterable<Annotation> getConstraints()
    {
        return constraints;
    }
}
