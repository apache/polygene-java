package org.qi4j.api.scala;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class ScalaTraitInvocationHandler
    implements InvocationHandler
{
    private final Method traitMethod;

    public ScalaTraitInvocationHandler( Method traitMethod )
    {
        this.traitMethod = traitMethod;
    }

    @Override
    public Object invoke( Object composite, Method method, Object[] args )
        throws Throwable
    {

        if( args != null )
        {
            Object[] params = new Object[ args.length + 1 ];
            params[ 0 ] = composite;
            System.arraycopy( args, 0, params, 1, args.length );

            return traitMethod.invoke( null, params );
        }
        else
        {
            return traitMethod.invoke( null, composite );
        }
    }
}
