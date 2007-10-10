package org.qi4j.api.model;

import java.lang.reflect.Method;

/**
 * TODO
 */
public class MethodModel
{
    Method method;

    public MethodModel( Method method )
    {
        this.method = method;
    }

    public Method getMethod()
    {
        return method;
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
