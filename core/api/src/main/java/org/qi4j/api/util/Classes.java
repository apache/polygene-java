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
package org.qi4j.api.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;

import static org.qi4j.functional.Iterables.cast;
import static org.qi4j.functional.Iterables.empty;
import static org.qi4j.functional.Iterables.flatten;
import static org.qi4j.functional.Iterables.flattenIterables;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.matchesAny;
import static org.qi4j.functional.Iterables.prepend;

/**
 * Useful methods for handling Classes.
 */
public final class Classes
{
    private final static Map<Type, Type> wrapperClasses = new HashMap<>();

    static
    {
        wrapperClasses.put( boolean.class, Boolean.class );
        wrapperClasses.put( byte.class, Byte.class );
        wrapperClasses.put( short.class, Short.class );
        wrapperClasses.put( char.class, Character.class );
        wrapperClasses.put( int.class, Integer.class );
        wrapperClasses.put( long.class, Long.class );
        wrapperClasses.put( float.class, Float.class );
        wrapperClasses.put( double.class, Double.class );
    }

    private final static Map<Type, Type> primitiveClasses = new HashMap<>();

    static
    {
        primitiveClasses.put( boolean.class, Boolean.class );
        primitiveClasses.put( byte.class, Byte.class );
        primitiveClasses.put( short.class, Short.class );
        primitiveClasses.put( char.class, Character.class );
        primitiveClasses.put( int.class, Integer.class );
        primitiveClasses.put( long.class, Long.class );
        primitiveClasses.put( float.class, Float.class );
        primitiveClasses.put( double.class, Double.class );
    }

    /**
     * Convert from primitive class (int, short, double, etc.) to wrapper class (Integer, Short, Double, etc.).
     * Return the same class if it's not a primitive class. This can therefore safely be used on all types
     * to ensure that they are not primitives.
     */
    private static final Function<Type, Type> WRAPPER_CLASS = new Function<Type, Type>()
    {
        @Override
        public Type map( Type aClass )
        {
            Type wrapperClass = wrapperClasses.get( aClass );
            return wrapperClass == null ? aClass : wrapperClass;
        }
    };

    /**
     * Convert from wrapper class (Integer, Short, Double, etc.) to primitive class (int, short, double, etc.).
     * Return the same class if it's not a wrapper class. This can therefore safely be used on all types
     * to ensure that they are primitives if possible.
     */
    private static final Function<Type, Type> PRIMITIVE_CLASS = new Function<Type, Type>()
    {
        @Override
        public Type map( Type aClass )
        {
            Type primitiveClass = primitiveClasses.get( aClass );
            return primitiveClass == null ? aClass : primitiveClass;
        }
    };

    /**
     * Function that extract the raw class of a type.
     */
    public static final Function<Type, Class<?>> RAW_CLASS = new Function<Type, Class<?>>()
    {
        @Override
        public Class<?> map( Type genericType )
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
            else if( genericType instanceof WildcardType )
            {
                return (Class<?>) ( (WildcardType) genericType ).getUpperBounds()[ 0];
            }
            else if( genericType instanceof GenericArrayType )
            {
                Object temp = Array.newInstance( (Class<?>) ( (GenericArrayType) genericType ).getGenericComponentType(), 0 );
                return temp.getClass();
            }
            throw new IllegalArgumentException( "Could not extract the raw class of " + genericType );
        }
    };

    private static final Function<AccessibleObject, Type> TYPE_OF = new Function<AccessibleObject, Type>()
    {
        @Override
        public Type map( AccessibleObject accessor )
        {
            return accessor instanceof Method ? ( (Method) accessor ).getGenericReturnType() : ( (Field) accessor ).getGenericType();
        }
    };

    private static final Function<Type, Iterable<Class<?>>> CLASS_HIERARCHY = new Function<Type, Iterable<Class<?>>>()
    {
        @Override
        @SuppressWarnings( {"raw", "unchecked"} )
        public Iterable<Class<?>> map( Type type )
        {
            if( type == null )
            {
                return empty();
            }
            if( type.equals( Object.class ) )
            {
                Class<?> aClass = (Class<?>) type;
                return cast( iterable( aClass ) );
            }
            else
            {
                type = RAW_CLASS.map( type );
                Class superclass = ( (Class) type ).getSuperclass();
                return prepend( (Class<?>) type, map( superclass ) );
            }
        }
    };

    @SuppressWarnings( "raw" )
    private static final Function<Type, Iterable<Type>> INTERFACES_OF = new Function<Type, Iterable<Type>>()
    {
        @Override
        public Iterable<Type> map( Type type )
        {
            Class clazz = RAW_CLASS.map( type );

            if( clazz.isInterface() )
            {
                Iterable<Type> genericInterfaces = iterable( clazz.getGenericInterfaces() );
                Iterable<Type> flattenIterables = flattenIterables( Iterables.map( INTERFACES_OF, genericInterfaces ) );
                return prepend( type, flattenIterables );
            }
            else
            {
                if( type.equals( Object.class ) )
                {
                    return iterable( clazz.getGenericInterfaces() );
                }
                else
                {
                    return flatten( flattenIterables( Iterables.map( INTERFACES_OF,
                                                                     iterable( clazz.getGenericInterfaces() ) ) ),
                                    INTERFACES_OF.map( RAW_CLASS.map( type ).getSuperclass() ) );
                }
            }
        }
    };

    @SuppressWarnings( "raw" )
    private static final Function<Type, Iterable<Type>> TYPES_OF = new Function<Type, Iterable<Type>>()
    {
        @Override
        public Iterable<Type> map( Type type )
        {
            Class clazz = RAW_CLASS.map( type );

            if( clazz.isInterface() )
            {
                Iterable<Type> genericInterfaces = iterable( clazz.getGenericInterfaces() );
                Iterable<Type> flattenIterables = flattenIterables( Iterables.map( INTERFACES_OF, genericInterfaces ) );
                return prepend( clazz, flattenIterables );
            }
            else
            {
                return flatten( CLASS_HIERARCHY.map( type ),
                                flattenIterables( Iterables.map( INTERFACES_OF, CLASS_HIERARCHY.map( type ) ) ) );
            }
        }
    };

    public static Type typeOf( AccessibleObject from )
    {
        return TYPE_OF.map( from );
    }

    public static Iterable<Type> typesOf( Iterable<Type> types )
    {
        Iterable<Type> result = empty();
        for( Type type : types )
        {
            result = flatten( result, typesOf( type ) );
        }
        return result;
    }

    public static Iterable<Type> typesOf( Type type )
    {
        return TYPES_OF.map( type );
    }

    public static Iterable<? extends Type> interfacesOf( Iterable<? extends Type> types )
    {
        Iterable<Type> result = empty();
        for( Type type : types )
        {
            result = flatten( result, interfacesOf( type ) );
        }
        return result;
    }

    public static Iterable<Type> interfacesOf( Type type )
    {
        return INTERFACES_OF.map( type );
    }

    public static Iterable<Class<?>> classHierarchy( Class<?> type )
    {
        return CLASS_HIERARCHY.map( type );
    }

    public static Type wrapperClass( Type type )
    {
        return WRAPPER_CLASS.map( type );
    }

    public static Specification<Class<?>> isAssignableFrom( final Class clazz )
    {
        return new Specification<Class<?>>()
        {
            @Override
            @SuppressWarnings( "unchecked" )
            public boolean satisfiedBy( Class<?> item )
            {
                return clazz.isAssignableFrom( item );
            }
        };
    }

    @SuppressWarnings( "raw" )
    public static Specification<Object> instanceOf( final Class clazz )
    {
        return new Specification<Object>()
        {
            @Override
            public boolean satisfiedBy( Object item )
            {
                return clazz.isInstance( item );
            }
        };
    }

    public static Specification<Class<?>> hasModifier( final int classModifier )
    {
        return new Specification<Class<?>>()
        {
            @Override
            public boolean satisfiedBy( Class<?> item )
            {
                return ( item.getModifiers() & classModifier ) != 0;
            }
        };
    }

    public static <T> Function<Type, Iterable<T>> forClassHierarchy( final Function<Class<?>, Iterable<T>> function )
    {
        return new Function<Type, Iterable<T>>()
        {
            @Override
            public Iterable<T> map( Type type )
            {
                return flattenIterables( Iterables.map( function, CLASS_HIERARCHY.map( type ) ) );
            }
        };
    }

    public static <T> Function<Type, Iterable<T>> forTypes( final Function<Type, Iterable<T>> function )
    {
        return new Function<Type, Iterable<T>>()
        {
            @Override
            public Iterable<T> map( Type type )
            {
                return flattenIterables( Iterables.map( function, TYPES_OF.map( type ) ) );
            }
        };
    }

    @SuppressWarnings( "raw" )
    public static Set<Class<?>> interfacesWithMethods( Set<Class<?>> interfaces )
    {
        Set<Class<?>> newSet = new LinkedHashSet<>();
        for( Class type : interfaces )
        {
            if( type.isInterface() && type.getDeclaredMethods().length > 0 )
            {
                newSet.add( type );
            }
        }

        return newSet;
    }

    public static String simpleGenericNameOf( Type type )
    {
        StringBuilder sb = new StringBuilder();
        simpleGenericNameOf( sb, type );
        return sb.toString();
    }

    @SuppressWarnings( "raw" )
    private static void simpleGenericNameOf( StringBuilder sb, Type type )
    {
        if( type instanceof Class )
        {
            sb.append( ( (Class) type ).getSimpleName() );
        }
        else if( type instanceof ParameterizedType )
        {
            ParameterizedType pt = (ParameterizedType) type;
            simpleGenericNameOf( sb, pt.getRawType() );
            sb.append( "<" );
            boolean atLeastOne = false;
            for( Type typeArgument : pt.getActualTypeArguments() )
            {
                if( atLeastOne )
                {
                    sb.append( ", " );
                }
                simpleGenericNameOf( sb, typeArgument );
                atLeastOne = true;
            }
            sb.append( ">" );
        }
        else if( type instanceof GenericArrayType )
        {
            GenericArrayType gat = (GenericArrayType) type;
            simpleGenericNameOf( sb, gat.getGenericComponentType() );
            sb.append( "[]" );
        }
        else if( type instanceof TypeVariable )
        {
            TypeVariable tv = (TypeVariable) type;
            sb.append( tv.getName() );
        }
        else if( type instanceof WildcardType )
        {
            WildcardType wt = (WildcardType) type;
            sb.append( "? extends " );
            boolean atLeastOne = false;
            for( Type typeArgument : wt.getUpperBounds() )
            {
                if( atLeastOne )
                {
                    sb.append( ", " );
                }
                simpleGenericNameOf( sb, typeArgument );
                atLeastOne = true;
            }
        }
        else
        {
            throw new IllegalArgumentException( "Don't know how to deal with type:" + type );
        }
    }

    public static <AnnotationType extends Annotation>
        AnnotationType findAnnotationOfTypeOrAnyOfSuperTypes( Class<?> type, Class<AnnotationType> annotationClass )
    {
        AnnotationType result = null;
        for( Type clazz : Classes.TYPES_OF.map( type ) )
        {
            result = Classes.RAW_CLASS.map( clazz ).getAnnotation( annotationClass );
            if( result != null )
            {
                break;
            }
        }

        return result;
    }

    public static Specification<Member> memberNamed( final String name )
    {
        return new Specification<Member>()
        {
            @Override
            public boolean satisfiedBy( Member item )
            {
                return item.getName().equals( name );
            }
        };
    }

    /**
     * Given a type variable, find what it resolves to given the declaring class where type
     * variable was found and a top class that extends the declaring class.
     *
     * @param name
     * @param declaringClass
     * @param topClass
     *
     * @return
     */
    @SuppressWarnings( "raw" )
    public static Type resolveTypeVariable( TypeVariable name, Class declaringClass, Class topClass )
    {
        Type type = resolveTypeVariable( name, declaringClass, new HashMap<TypeVariable, Type>(), topClass );
        if( type == null )
        {
            type = Object.class;
        }
        return type;
    }

    @SuppressWarnings( "raw" )
    private static Type resolveTypeVariable( TypeVariable name,
                                             Class declaringClass,
                                             Map<TypeVariable, Type> mappings,
                                             Class current
    )
    {
        if( current.equals( declaringClass ) )
        {
            Type resolvedType = name;
            while( resolvedType instanceof TypeVariable )
            {
                resolvedType = mappings.get( resolvedType );
            }
            return resolvedType;
        }

        List<Type> types = new ArrayList<>();
        for( Type type : current.getGenericInterfaces() )
        {
            Iterable<Type> interfaces = Classes.INTERFACES_OF.map( type );
            for( Type anInterface : interfaces )
            {
                if( !types.contains( anInterface ) )
                {
                    types.add( anInterface );
                }
            }
            types.add( type );
        }

        if( current.getGenericSuperclass() != null )
        {
            types.add( current.getGenericSuperclass() );
        }

        for( Type type : types )
        {
            Class subClass;
            if( type instanceof ParameterizedType )
            {
                ParameterizedType pt = (ParameterizedType) type;
                Type[] args = pt.getActualTypeArguments();
                Class clazz = (Class) pt.getRawType();
                TypeVariable[] vars = clazz.getTypeParameters();
                for( int i = 0; i < vars.length; i++ )
                {
                    TypeVariable var = vars[ i];
                    Type mappedType = args[ i];
                    mappings.put( var, mappedType );
                }
                subClass = (Class) pt.getRawType();
            }
            else
            {
                subClass = (Class) type;
            }

            Type resolvedType = resolveTypeVariable( name, declaringClass, mappings, subClass );
            if( resolvedType != null )
            {
                return resolvedType;
            }
        }

        return null;
    }

    /**
     * Get URI for a class.
     *
     * @param clazz class
     *
     * @return URI
     *
     * @throws NullPointerException if clazz is null
     */
    @SuppressWarnings( "raw" )
    public static String toURI( final Class clazz )
        throws NullPointerException
    {
        return toURI( clazz.getName() );
    }

    /**
     * Get URI for a class name.
     * <p/>
     * Example:
     * Class name com.example.Foo$Bar
     * is converted to
     * URI urn:qi4j:com.example.Foo-Bar
     *
     * @param className class name
     *
     * @return URI
     *
     * @throws NullPointerException if className is null
     */
    public static String toURI( String className )
        throws NullPointerException
    {
        className = normalizeClassToURI( className );
        return "urn:qi4j:type:" + className;
    }

    /**
     * Get class name from a URI
     *
     * @param uri URI
     *
     * @return class name
     *
     * @throws NullPointerException if uri is null
     */
    public static String toClassName( String uri )
        throws NullPointerException
    {
        uri = uri.substring( "urn:qi4j:type:".length() );
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

    public static Specification<ModelDescriptor> modelTypeSpecification( final String className )
    {
        return new Specification<ModelDescriptor>()
        {
            @Override
            public boolean satisfiedBy( ModelDescriptor item )
            {
                return matchesAny( new Specification<String>()
                {
                    @Override
                    public boolean satisfiedBy( String item )
                    {
                        return item.equals( className );
                    }
                }, map( new Function<Class<?>, String>()
                {
                    @Override
                    public String map( Class<?> item )
                    {
                        return item.getName();
                    }
                }, item.types() ) );
            }
        };
    }

    @SuppressWarnings( "raw" )
    public static Specification<ModelDescriptor> exactTypeSpecification( final Class type )
    {
        return new Specification<ModelDescriptor>()
        {
            @Override
            public boolean satisfiedBy( ModelDescriptor item )
            {
                return matchesAny( new Specification<Class<?>>()
                {
                    @Override
                    public boolean satisfiedBy( Class<?> item )
                    {
                        return item.equals( type );
                    }
                }, item.types() );
            }
        };
    }

    @SuppressWarnings( "raw" )
    public static Specification<ModelDescriptor> assignableTypeSpecification( final Class type )
    {
        return new Specification<ModelDescriptor>()
        {
            @Override
            public boolean satisfiedBy( ModelDescriptor item )
            {
                return matchesAny( new Specification<Class<?>>()
                {
                    @Override
                    @SuppressWarnings( "unchecked" )
                    public boolean satisfiedBy( Class<?> itemType )
                    {
                        return !type.equals( itemType ) && type.isAssignableFrom( itemType );
                    }
                }, item.types() );
            }
        };
    }

    @SuppressWarnings( "raw" )
    public static String toString( Iterable<? extends Class> type )
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "[" );
        boolean first = true;
        for( Class c : type )
        {
            if( !first )
            {
                builder.append( "," );
            }
            first = false;
            builder.append( c.getSimpleName() );
        }
        builder.append( "]" );
        return builder.toString();
    }

    public static Function<Type, String> toClassName()
    {
        return new Function<Type, String>()
        {
            @Override
            public String map( Type type )
            {
                return RAW_CLASS.map( type ).getName();
            }
        };
    }

    private Classes()
    {
    }
}
