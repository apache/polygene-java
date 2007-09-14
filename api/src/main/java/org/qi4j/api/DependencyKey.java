package org.qi4j.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Dependency key used for dependency resolutions. The key is comprised
 * of the thisAs type, the fragment type, the annotation type, the generic dependency type, and an optional name.
 */
public class DependencyKey
{
    private Class<? extends Annotation> annotationType;
    private Type genericType;
    private String name;
    private Class fragmentType;
    private Class compositeType;

    public DependencyKey( Class<? extends Annotation> annotationType, Type genericType, String name, Class fragmentType, Class compositeType )
    {
        this.compositeType = compositeType;
        this.fragmentType = fragmentType;
        this.annotationType = annotationType;
        this.genericType = genericType;
        this.name = name;
    }

    public Class<? extends Annotation> getAnnotationType()
    {
        return annotationType;
    }

    public Type getGenericType()
    {
        return genericType;
    }

    public Class getRawClass()
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

    public Class getFragmentType()
    {
        return fragmentType;
    }

    public Class getCompositeType()
    {
        return compositeType;
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

        DependencyKey that = (DependencyKey) o;

        if( !genericType.equals( that.genericType ) )
        {
            return false;
        }
        if( !annotationType.equals( ( that.annotationType ) ) )
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
        return compositeType.getSimpleName() + ":" + fragmentType.getSimpleName() + ":" + annotationType.getName() + ":" + genericType + ( name == null ? "" : ":" + name );
    }
}
