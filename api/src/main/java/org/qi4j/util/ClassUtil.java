/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.property.ComputedPropertyInstance;

/**
 * Class-related utility methods
 */
public class ClassUtil
{
    public static <T> T propertyMethodOf( Class<T> type )
    {
        return (T) Proxy.newProxyInstance( type.getClassLoader(), new Class[]{ type }, new InvocationHandler()
        {
            public Object invoke( Object o, final Method method, Object[] objects ) throws Throwable
            {
                return new ComputedPropertyInstance( method )
                {
                    public Object get()
                    {
                        return method;
                    }
                };
            }
        } );
    }

    /**
     * Get all interfaces for the given type,
     * including the provided type. No type
     * is included twice in the list.
     *
     * @param type to extract interfaces from
     * @return set of interfaces of given type
     */
    public static Set<Class> interfacesOf( Type type )
    {
        Set<Class> interfaces = new LinkedHashSet<Class>();
        addInterfaces( type, interfaces );

        if( type instanceof Class )
        {
            Class current = (Class) type;
            while( current != null )
            {
                addInterfaces( current, interfaces );
                current = current.getSuperclass();
            }
        }

        return interfaces;
    }

    /**
     * Get all interfaces for the given type,
     * including the provided type. No type
     * is included twice in the list.
     *
     * @param type to extract interfaces from
     * @return set of interfaces of given type
     */
    public static Set<Type> genericInterfacesOf( Type type )
    {
        Set<Type> interfaces = new LinkedHashSet<Type>();
        addGenericInterfaces( type, interfaces );

        if( type instanceof Class )
        {
            Class current = (Class) type;
            while( current != null )
            {
                addGenericInterfaces( current, interfaces );
                current = current.getSuperclass();
            }
        }

        return interfaces;
    }

    public static Set<Class> interfacesWithMethods( Set<Class> interfaces )
    {
        Set<Class> newSet = new LinkedHashSet<Class>();
        for( Class type : interfaces )
        {
            if( type.isInterface() && type.getDeclaredMethods().length > 0 )
            {
                newSet.add( type );
            }
        }

        return newSet;
    }

    public static Set<Class> classesOf( Type type )
    {
        Set<Class> types = new LinkedHashSet<Class>();
        addInterfaces( type, types );

        if( type instanceof Class )
        {
            Class current = (Class) type;
            while( current != null )
            {
                types.add( current );
                current = current.getSuperclass();
            }
        }

        return types;
    }


    public static Class[] toClassArray( Set<Class> types )
    {
        Class[] array = new Class[types.size()];
        int idx = 0;
        for( Class type : types )
        {
            array[ idx++ ] = type;
        }

        return array;
    }

    public static Type actualTypeOf( Type type )
    {
        Set<Class> types = interfacesOf( type );
        for( Type type1 : types )
        {
            if( type1 instanceof ParameterizedType )
            {
                return ( (ParameterizedType) type1 ).getActualTypeArguments()[ 0 ];
            }
        }
        return null;
    }

    public static Class<?> getRawClass( final Type genericType )
    {
        // Calculate raw type
        if( genericType instanceof Class )
        {
            return (Class<?>) genericType;
        }
        else if( genericType instanceof ParameterizedType )
        {
            return (Class<?>) ( (ParameterizedType) genericType ).getRawType();
        }
        else if( genericType instanceof TypeVariable )
        {
            return (Class<?>) ( (TypeVariable) genericType ).getGenericDeclaration();
        }
        else if( genericType instanceof GenericArrayType )
        {
            Object temp = Array.newInstance( (Class<?>)((GenericArrayType) genericType).getGenericComponentType(), 0 );
            return  temp.getClass();
        }
        throw new IllegalArgumentException( "Could not extract the raw class of " + genericType );
    }

    public static List<Constructor> constructorsOf( Class clazz )
    {
        List<Constructor> constructors = new ArrayList<Constructor>();
        addConstructors( clazz, constructors );
        return constructors;
    }

    private static void addConstructors( Class clazz, List<Constructor> constructors )
    {
        if( clazz != null && !clazz.equals( Object.class ) )
        {
            constructors.addAll( asList( clazz.getDeclaredConstructors() ) );
            addConstructors( clazz.getSuperclass(), constructors );
        }
    }

    public static List<Method> methodsOf( Class clazz )
    {
        List<Method> methods = new ArrayList<Method>();
        addMethods( clazz, methods );
        return methods;
    }

    private static void addMethods( Class clazz, List<Method> methods )
    {
        if( clazz != null && !clazz.equals( Object.class ) )
        {
            methods.addAll( asList( clazz.getDeclaredMethods() ) );
            addMethods( clazz.getSuperclass(), methods );
        }
    }

    public static List<Field> fieldsOf( Class clazz )
    {
        List<Field> fields = new ArrayList<Field>();
        addFields( clazz, fields );
        return fields;
    }

    private static void addFields( Class clazz, List<Field> fields )
    {
        if( clazz != null && !clazz.equals( Object.class ) )
        {
            fields.addAll( asList( clazz.getDeclaredFields() ) );
            addFields( clazz.getSuperclass(), fields );
        }
    }

    private static void addInterfaces( Type type, Set<Class> interfaces )
    {
        if( !interfaces.contains( type ) )
        {
            if( type instanceof ParameterizedType )
            {
                final ParameterizedType parameterizedType = (ParameterizedType) type;
                addInterfaces( parameterizedType.getRawType(), interfaces );
            }
            else if( type instanceof Class )
            {
                Class clazz = (Class) type;

                if( clazz.isInterface() )
                {
                    interfaces.add( clazz );
                }

                Type[] subTypes = clazz.getGenericInterfaces();
                for( Type subType : subTypes )
                {
                    addInterfaces( subType, interfaces );
                }
            }
        }
    }

    private static void addGenericInterfaces( Type type, Set<Type> interfaces )
    {
        if( !interfaces.contains( type ) )
        {
            if( type instanceof ParameterizedType )
            {
                interfaces.add( type );
            }
            else if( type instanceof Class )
            {
                Class clazz = (Class) type;

                if( clazz.isInterface() )
                {
                    interfaces.add( clazz );
                }

                Type[] subTypes = clazz.getGenericInterfaces();
                for( Type subType : subTypes )
                {
                    addGenericInterfaces( subType, interfaces );
                }
            }
        }
    }

    /**
     * Get URI for a class.
     *
     * @param clazz class
     * @return URI
     * @throws NullPointerException if clazz is null
     */
    public static String toURI( final Class clazz )
        throws NullPointerException
    {
        return toURI( clazz.getName() );
    }

    /**
     * Get URI for a class name.
     *
     * Example:
     * Class name com.example.Foo$Bar
     * is converted to
     * URI urn:qi4j:com.example.Foo-Bar
     *
     * @param className class name
     * @return URI
     * @throws NullPointerException if className is null
     */
    public static String toURI( String className )
        throws NullPointerException
    {
        className = normalizeClassToURI( className );
        return "urn:qi4j:entity:" + className;
    }

    /**
     * Get class name from a URI
     *
     * @param uri URI
     * @return class name
     * @throws NullPointerException if uri is null
     */
    public static String toClassName( String uri )
        throws NullPointerException
    {
        uri = uri.substring( "urn:qi4j:entity:".length() );
        uri = denormalizeURIToClass( uri );
        return uri;
    }

    public static String normalizeClassToURI( String className )
    {
        return className.replace( '$', '-' );
    }

    public static String denormalizeURIToClass( String uriPart )
    {
        return uriPart.replace( '-', '$' );
    }

}
