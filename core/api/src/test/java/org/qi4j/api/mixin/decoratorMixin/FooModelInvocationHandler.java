package org.qi4j.api.mixin.decoratorMixin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FooModelInvocationHandler
    implements InvocationHandler
{
    private String value;

    public FooModelInvocationHandler( String value )
    {
        this.value = value;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if(method.getName().equals( "hashCode" ))
            return hashCode();
        if(method.getName().equals( "equals" ))
            return equals(args[0]);
        if(args==null || args.length==0)
            return value;
        value = (String) args[0];
        return null;
    }
}
