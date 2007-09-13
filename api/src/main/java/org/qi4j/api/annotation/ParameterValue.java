package org.qi4j.api.annotation;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * TODO
 */
public class ParameterValue
{
    private static ThreadLocal<String> currentName = new ThreadLocal<String>();

    public static ParameterValue parameter(String name, Object value)
    {
        if (name == null)
            return parameter((Object)null, value);
        
        return new ParameterValue( name, value);
    }

    public static ParameterValue parameter(Object nullName, Object value)
    {
        String name = currentName.get();
        if (name == null)
            throw new IllegalStateException("You must set a name by calling name() first");
        currentName.remove();

        return new ParameterValue( name, value);
    }

    public static <T> T name( Class<? extends T> mixinType )
    {
        InvocationHandler ih = new ParameterNameInvocationHandler();
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[] {mixinType}, ih));
    }

    String name;
    Object value;

    public ParameterValue( String name, Object value )
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
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

        ParameterValue that = (ParameterValue) o;

        if( !name.equals( that.name ) )
        {
            return false;
        }
        if( value != null ? !value.equals( that.value ) : that.value != null )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    @Override public String toString()
    {
        return name+"="+value;
    }

    static class ParameterNameInvocationHandler
        implements InvocationHandler
    {
        ParameterNameInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            String name = Introspector.decapitalize( method.getName().substring( 3 ) );
            currentName.set( name );
            return null;
        }
    }
}
