package org.qi4j.runtime.injection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * TODO
 */
public class ParameterizedTypeInstance
    implements ParameterizedType
{
    private Type[] actualTypeArguments;
    private Type rawType;
    private Type ownerType;

    public ParameterizedTypeInstance( Type[] actualTypeArguments, Type rawType, Type ownerType )
    {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = ownerType;
    }

    @Override
    public Type[] getActualTypeArguments()
    {
        return actualTypeArguments;
    }

    @Override
    public Type getRawType()
    {
        return rawType;
    }

    @Override
    public Type getOwnerType()
    {
        return ownerType;
    }

    @Override
    public String toString()
    {
        return rawType.toString() + Arrays.asList( actualTypeArguments );
    }
}
