package org.qi4j.api.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Injection key used for dependency injections. The key is comprised
 * of the thisAs type, the fragment type, the annotation type, the generic dependency type, and an optional name.
 */
public class InjectionKey
{
    public static InjectionKey key( Type genericType, String name, Class dependentType )
    {
        return new InjectionKey( genericType, name, dependentType );
    }

    public static InjectionKey key( Type genericType, String name )
    {
        return new InjectionKey( genericType, name, null );
    }

    public static InjectionKey key( Type genericType )
    {
        return new InjectionKey( genericType, null, null );
    }

    public static InjectionKey key( final Type rawType, final Type parameterType )
    {
        Type type = new ParameterizedType()
        {
            public Type[] getActualTypeArguments()
            {
                return new Type[]{ parameterType };
            }

            public Type getRawType()
            {
                return rawType;
            }

            public Type getOwnerType()
            {
                return null;
            }

            @Override public String toString()
            {
                return rawType + "<" + parameterType + ">";
            }
        };

        return new InjectionKey( type, null, null );
    }

    public static InjectionKey key( Object value, String name )
    {
        return new InjectionKey( value.getClass(), name, null );
    }

    private Type genericType;
    private String name;
    private Class dependentType;

    public InjectionKey( Type genericType, String name, Class dependentType )
    {
        this.dependentType = dependentType;
        this.genericType = genericType;
        this.name = name;
    }

    public Type getGenericType()
    {
        return genericType;
    }

    /**
     * Get the raw dependency type. If the dependency uses generics this is the raw type,
     * and otherwise it is the type of the field. Examples:
     *
     * @return
     * @Service MyService service -> MyService
     * @Entity Iterable<Foo> fooList -> Iterable
     * @Entity Query<Foo> fooQuery -> Query
     */
    public Class getRawType()
    {
        if( genericType instanceof Class )
        {
            return (Class) genericType;
        }
        else if( genericType instanceof ParameterizedType )
        {
            return (Class) ( (ParameterizedType) genericType ).getRawType();
        }
        else
        {
            return null; // TODO can this happen?
        }
    }

    /**
     * Get the dependency type. If the dependency uses generics this is the parameter type,
     * and otherwise it is the raw type. Examples:
     *
     * @return
     * @Service MyService service -> MyService
     * @Entity Iterable<Foo> fooList -> Foo
     * @Entity Query<Foo> fooQuery -> Foo
     */
    public Class getDependencyType()
    {
        if( genericType instanceof ParameterizedType )
        {
            return (Class) ( (ParameterizedType) genericType ).getActualTypeArguments()[ 0 ];
        }
        else
        {
            return (Class) genericType;
        }
    }

    public String getName()
    {
        return name;
    }

    public Class getDependentType()
    {
        return dependentType;
    }

    @Override public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        InjectionKey that = (InjectionKey) o;

        if( !genericType.equals( that.genericType ) )
        {
            return false;
        }
        if( name != null ? !name.equals( that.name ) : that.name != null )
        {
            return false;
        }

        return true;
    }

    @Override public int hashCode()
    {
        int result;
        result = genericType.hashCode();
        result = 31 * result + ( name != null ? name.hashCode() : 0 );
        return result;
    }

    @Override public String toString()
    {
        return ( dependentType == null ? "" : dependentType.getSimpleName() + ":" ) + genericType + ( name == null ? "" : ":" + name );
    }
}