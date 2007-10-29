package org.qi4j.model;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class MethodModel
{
    private Method method;
    private MethodConstraint methodConstraint;

    public MethodModel( Method method, MethodConstraint methodConstraint )
    {
        this.methodConstraint = methodConstraint;
        this.method = method;
    }

    public Method getMethod()
    {
        return method;
    }

    public MethodConstraint getMethodConstraint()
    {
        return methodConstraint;
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

        MethodModel that = (MethodModel) o;

        if( !method.equals( that.method ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return method.hashCode();
    }


    @Override public String toString()
    {
        return method.toGenericString();
    }
}
