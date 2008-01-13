package org.qi4j.library.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Generic mixin that is a no-op. Can be useful if the functionality
 * of a method is mainly provided by concerns and side-effects.
 */
public class NoopMixin
    implements InvocationHandler
{
    private static final Boolean BOOLEAN_DEFAULT = Boolean.FALSE;
    private static final Short SHORT_DEFAULT = 0;
    private static final Character CHARACTER_DEFAULT = 0;
    private static final Integer INTEGER_DEFAULT = 0;
    private static final Long LONG_DEFAULT = 0L;
    private static final Float FLOAT_DEFAULT = 0f;
    private static final Double DOUBLE_DEFAULT = 0.0;

    public Object invoke( Object object, Method method, Object[] args )
        throws Throwable
    {
        Class retType = method.getReturnType();
        if( retType.isPrimitive() )
        {
            if( Void.TYPE == retType )
            {
                return null;
            }
            if( Boolean.TYPE == retType )
            {
                return BOOLEAN_DEFAULT;
            }
            if( Short.TYPE == retType )
            {
                return SHORT_DEFAULT;
            }
            if( Character.TYPE == retType )
            {
                return CHARACTER_DEFAULT;
            }
            if( Integer.TYPE == retType )
            {
                return INTEGER_DEFAULT;
            }
            if( Long.TYPE == retType )
            {
                return LONG_DEFAULT;
            }
            if( Float.TYPE == retType )
            {
                return FLOAT_DEFAULT;
            }
            if( Double.TYPE == retType )
            {
                return DOUBLE_DEFAULT;
            }
        }
        return null;
    }
}
