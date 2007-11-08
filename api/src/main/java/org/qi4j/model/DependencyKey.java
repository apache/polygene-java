/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Dependency key used for dependency resolutions. The key is comprised
 * of the thisAs type, the fragment type, the annotation type, the generic dependency type, and an optional name.
 */
public class DependencyKey
{
    static private Map<Class, Class> wrapperClasses = new HashMap<Class, Class>();

    static
    {
        wrapperClasses.put( Integer.TYPE, Integer.class );
        wrapperClasses.put( Short.TYPE, Short.class );
        wrapperClasses.put( Long.TYPE, Long.class );
        wrapperClasses.put( Float.TYPE, Float.class );
        wrapperClasses.put( Double.TYPE, Double.class );
        wrapperClasses.put( Byte.TYPE, Byte.class );
        wrapperClasses.put( Boolean.TYPE, Boolean.class );
        wrapperClasses.put( Character.TYPE, Character.class );
    }

    private Class<? extends Annotation> annotationType;
    private Type genericType;
    private String name;
    private Class dependentType;

    public DependencyKey( Class<? extends Annotation> annotationType, Type genericType, String name, Class dependentType )
    {
        this.dependentType = dependentType;
        this.annotationType = annotationType;
        this.genericType = genericType;
        this.name = name;

        Class rawType = getRawType();
        if( rawType.isPrimitive() )
        {
            // Map primitive types to the wrapper classes
            this.genericType = wrapperClasses.get( rawType );
        }
    }

    public Class<? extends Annotation> getAnnotationType()
    {
        return annotationType;
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
        return ( dependentType == null ? "" : dependentType.getSimpleName() + ":" ) + annotationType.getName() + ":" + genericType + ( name == null ? "" : ":" + name );
    }
}
