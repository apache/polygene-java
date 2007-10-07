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
}
