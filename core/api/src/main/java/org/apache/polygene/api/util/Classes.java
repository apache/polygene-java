/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.util;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.composite.ModelDescriptor;

import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;

/**
 * Useful methods for handling Classes.
 */
public final class Classes
{
    private final static Map<Type, Type> WRAPPER_CLASSES = new HashMap<>();

    static
    {
        WRAPPER_CLASSES.put( boolean.class, Boolean.class );
        WRAPPER_CLASSES.put( byte.class, Byte.class );
        WRAPPER_CLASSES.put( short.class, Short.class );
        WRAPPER_CLASSES.put( char.class, Character.class );
        WRAPPER_CLASSES.put( int.class, Integer.class );
        WRAPPER_CLASSES.put( long.class, Long.class );
        WRAPPER_CLASSES.put( float.class, Float.class );
        WRAPPER_CLASSES.put( double.class, Double.class );
    }

    private final static Map<Type, Type> PRIMITIVE_CLASSES = new HashMap<>();

    static
    {
        PRIMITIVE_CLASSES.put( boolean.class, Boolean.class );
        PRIMITIVE_CLASSES.put( byte.class, Byte.class );
        PRIMITIVE_CLASSES.put( short.class, Short.class );
        PRIMITIVE_CLASSES.put( char.class, Character.class );
        PRIMITIVE_CLASSES.put( int.class, Integer.class );
        PRIMITIVE_CLASSES.put( long.class, Long.class );
        PRIMITIVE_CLASSES.put( float.class, Float.class );
        PRIMITIVE_CLASSES.put( double.class, Double.class );
    }

    /**
     * Convert from primitive class (int, short, double, etc.) to wrapper class (Integer, Short, Double, etc.).
     * Return the same class if it's not a primitive class. This can therefore safely be used on all types
     * to ensure that they are not primitives.
     */
    private static final Function<Type, Type> WRAPPER_CLASS = clazz -> {
        Type wrapperClass = WRAPPER_CLASSES.get( clazz );
        return wrapperClass == null ? clazz : wrapperClass;
    };

    /**
     * Convert from wrapper class (Integer, Short, Double, etc.) to primitive class (int, short, double, etc.).
     * Return the same class if it's not a wrapper class. This can therefore safely be used on all types
     * to ensure that they are primitives if possible.
     */
    @SuppressWarnings( "UnusedDeclaration" )
    private static final Function<Type, Type> PRIMITIVE_CLASS = aClass -> {
        Type primitiveClass = PRIMITIVE_CLASSES.get( aClass );
        return primitiveClass == null ? aClass : primitiveClass;
    };

    /**
     * Function that extract the raw class of a type.
     */
    public static final Function<Type, Class<?>> RAW_CLASS = genericType -> {
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
            return (Class<?>) ( (WildcardType) genericType ).getUpperBounds()[ 0 ];
        }
        else if( genericType instanceof GenericArrayType )
        {
            Object temp = Array.newInstance( (Class<?>) ( (GenericArrayType) genericType ).getGenericComponentType(), 0 );
            return temp.getClass();
        }
        throw new IllegalArgumentException( "Could not extract the raw class of " + genericType );
    };

    private static final Function<AccessibleObject, Type> TYPE_OF = accessor -> {
        if( accessor instanceof Method )
        {
            return ( (Method) accessor ).getGenericReturnType();
        }
        return ( (Field) accessor ).getGenericType();
    };

    private static final Function<Type, Stream<Class<?>>> CLASS_HIERARCHY = new Function<Type, Stream<Class<?>>>()
    {
        @Override
        public Stream<Class<?>> apply( Type type )
        {
            if( type == null )
            {
                return Stream.empty();
            }
            if( type.equals( Object.class ) )
            {
                return Stream.of( (Class<?>) type );
            }
            else
            {
                type = RAW_CLASS.apply( type );
                Class superclass = ( (Class) type ).getSuperclass();
                return concat( Stream.of( (Class<?>) type ), apply( superclass ) );
            }
        }
    };

    @SuppressWarnings( "raw" )
    private static final Function<Type, Stream<? extends Type>> INTERFACES_OF = new Function<Type, Stream<? extends Type>>()
    {
        @Override
        public Stream<? extends Type> apply( Type type )
        {
            Class clazz = RAW_CLASS.apply( type );

            if( clazz.isInterface() )
            {
                Stream<? extends Type> genericInterfaces = Arrays.stream( clazz.getGenericInterfaces() );
                Stream<? extends Type> intfaces = genericInterfaces.flatMap( INTERFACES_OF );
                return concat( Stream.of( type ), intfaces );
            }
            else
            {
                if( type.equals( Object.class ) )
                {
                    return Arrays.stream( clazz.getGenericInterfaces() );
                }
                else
                {
                    return concat( Stream.of( clazz.getGenericInterfaces() ).flatMap( INTERFACES_OF ),
                                   Stream.of( clazz.getSuperclass() ).flatMap( INTERFACES_OF ) );

                }
            }
        }
    };

    private static final Function<Type, Stream<? extends Type>> TYPES_OF = type -> {
        Class clazz = RAW_CLASS.apply( type );

        if( clazz.isInterface() )
        {
            Stream<Type> intfaces = Arrays.stream( clazz.getGenericInterfaces() ).flatMap( INTERFACES_OF );
            return concat( Stream.of( clazz ), intfaces );
        }
        else
        {
            return concat( Stream.of( clazz ),
                           Stream.of( type ).flatMap( CLASS_HIERARCHY ).flatMap( INTERFACES_OF ) );
        }
    };

    public static Type typeOf( AccessibleObject from )
    {
        return TYPE_OF.apply( from );
    }

    public static Stream<Type> typesOf( Stream<? extends Type> types )
    {
        return types.flatMap( TYPES_OF );
    }

    public static Stream<? extends Type> typesOf( Type type )
    {
        return TYPES_OF.apply( type );
    }

    public static Stream<? extends Type> interfacesOf( Stream<? extends Type> types )
    {
        return types.flatMap( INTERFACES_OF );
    }

    public static Stream<? extends Type> interfacesOf( Type type )
    {
        return Stream.of( type ).flatMap( INTERFACES_OF );
    }

    public static Stream<Class<?>> classHierarchy( Class<?> type )
    {
        return Stream.of( type ).flatMap( CLASS_HIERARCHY );
    }

    public static Type wrapperClass( Type type )
    {
        return WRAPPER_CLASS.apply( type );
    }

    public static Predicate<Class<?>> isAssignableFrom( final Class<?> clazz )
    {
        return clazz::isAssignableFrom;
    }

    public static Predicate<Object> instanceOf( final Class<?> clazz )
    {
        return clazz::isInstance;
    }

    public static Predicate<Class<?>> hasModifier( final int classModifier )
    {
        return item -> ( item.getModifiers() & classModifier ) != 0;
    }

    public static <T> Function<Type, Stream<T>> forClassHierarchy( final Function<Class<?>, Stream<T>> function )
    {
        return type -> Stream.of( type ).flatMap( CLASS_HIERARCHY ).flatMap( function );
    }

    public static <T> Function<Type, Stream<T>> forTypes( final Function<Type, Stream<T>> function )
    {
        return type -> Stream.of( type ).flatMap( TYPES_OF ).flatMap( function );
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

    @SuppressWarnings( "UnusedDeclaration" )
    public static <AnnotationType extends Annotation>
    AnnotationType findAnnotationOfTypeOrAnyOfSuperTypes( Class<?> type, Class<AnnotationType> annotationClass )
    {
        return Stream.of( type )
            .flatMap( TYPES_OF )
            .map( RAW_CLASS )
            .map( clazz -> clazz.getAnnotation( annotationClass ) )
            .filter( Objects::nonNull )
            .findAny().orElse( null );
    }

    public static Predicate<Member> memberNamed( final String name )
    {
        return item -> item.getName().equals( name );
    }

    /**
     * Given a type variable, find what it resolves to given the declaring class where type
     * variable was found and a top class that extends the declaring class.
     *
     * @param name           The TypeVariable name.
     * @param declaringClass The class where the TypeVariable is declared.
     * @param topClass       The top class that extends the declaringClass
     *
     * @return The Type instance of the given TypeVariable
     */
    @SuppressWarnings( "raw" )
    public static Type resolveTypeVariable( TypeVariable name, Class declaringClass, Class topClass )
    {
        Type type = resolveTypeVariable( name, declaringClass, new HashMap<>(), topClass );
        if( type == null )
        {
            type = Object.class;
        }
        return type;
    }

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

        Stream<? extends Type> stream = Arrays.stream( current.getGenericInterfaces() )
            .flatMap( INTERFACES_OF )
            .distinct();

        Type genericSuperclass = current.getGenericSuperclass();
        if( genericSuperclass != null )
        {
            stream = concat( stream, Stream.of( genericSuperclass ) );
        }
        return stream.map( type -> {
            Class subClass;
            if( type instanceof ParameterizedType )
            {
                subClass = extractTypeVariables( mappings, (ParameterizedType) type );
            }
            else
            {
                subClass = (Class) type;
            }
            return subClass;
        } )
            .map( subClass -> resolveTypeVariable( name, declaringClass, mappings, subClass ) )
            .filter( Objects::nonNull )
            .findAny().orElse( null );
    }

    private static Class extractTypeVariables( Map<TypeVariable, Type> mappings, ParameterizedType type )
    {
        Class subClass;
        Type[] args = type.getActualTypeArguments();
        Class clazz = (Class) type.getRawType();
        TypeVariable[] vars = clazz.getTypeParameters();
        for( int i = 0; i < vars.length; i++ )
        {
            TypeVariable var = vars[ i ];
            Type mappedType = args[ i ];
            mappings.put( var, mappedType );
        }
        subClass = (Class) type.getRawType();
        return subClass;
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
     * <p>
     * Example:
     * </p>
     * <p>
     * Class name com.example.Foo$Bar is converted to URI urn:polygene:com.example.Foo-Bar
     * </p>
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
        return "urn:polygene:type:" + className;
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
        uri = uri.substring( "urn:polygene:type:".length() );
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

    public static Predicate<ModelDescriptor> modelTypeSpecification( final String className )
    {
        return item ->
            stream( item.types().spliterator(), false )
                .map( Class::getName ).anyMatch( typeName -> typeName.equals( className ) );
    }

    @SuppressWarnings( "raw" )
    public static Predicate<ModelDescriptor> exactTypeSpecification( final Class type )
    {
        return item -> item.types().anyMatch( clazz -> clazz.equals( type ) );
    }

    @SuppressWarnings( "raw" )
    public static Predicate<ModelDescriptor> assignableTypeSpecification( final Class<?> type )
    {
        return item ->
            item.types().anyMatch(
                itemType -> !type.equals( itemType ) && type.isAssignableFrom( itemType )
            );
    }

    @SuppressWarnings( "raw" )
    public static String toString( Stream<? extends Class> types )
    {
        return "[" + types.map( Class::getSimpleName ).collect( Collectors.joining( "," ) ) + "]";
    }

    public static Function<Type, String> toClassName()
    {
        return type -> RAW_CLASS.apply( type ).getName();
    }

    private Classes()
    {
    }
}
