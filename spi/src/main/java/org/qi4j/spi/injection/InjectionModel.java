/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Injection model used as base for all types of injections. The model is comprised
 * of the the annotation, the generic dependency type, and the dependent type that declared the injection.
 */
public class InjectionModel
{
    private Class<? extends Annotation> injectionAnnotation;
    private Type injectionType;
    private Class injectedClass;
    private Class rawInjectionType;
    private Class injectionClass;
    protected boolean optional;

    public InjectionModel( Class<? extends Annotation> injectionAnnotation, Type genericType, Class injectedClass, boolean optional )
    {
        this.injectionAnnotation = injectionAnnotation;
        this.injectedClass = injectedClass;
        this.injectionType = genericType;
        this.optional = optional;

        // Calculate raw injection type
        if( injectionType instanceof Class )
        {
            rawInjectionType = (Class) injectionType;
        }
        else if( injectionType instanceof ParameterizedType )
        {
            rawInjectionType = (Class) ( (ParameterizedType) injectionType ).getRawType();
        }
        else if( injectionType instanceof TypeVariable )
        {
            int index = 0;
            TypeVariable<?>[] typeVariables = ( (TypeVariable) injectionType ).getGenericDeclaration().getTypeParameters();
            for( TypeVariable typeVariable : typeVariables )
            {
                if( "T".equals( typeVariable.getName() ) )
                {
                    Type genericSuperclass = injectedClass.getGenericSuperclass();
                    Type type;
                    if( genericSuperclass instanceof ParameterizedType )
                    {
                        type = ( (ParameterizedType) genericSuperclass ).getActualTypeArguments()[ index ];
                    }
                    else
                    {
                        type = ((Class) genericSuperclass).getGenericInterfaces()[index++];
                    }
                    rawInjectionType = (Class) type;
                }
                index++;
            }
        }

        if( rawInjectionType.isPrimitive() )
        {
            rawInjectionType = getNonPrimitiveType( rawInjectionType );
        }

        // Calculate injection class
        if( injectionType instanceof ParameterizedType )
        {
            Type type = ( (ParameterizedType) injectionType ).getActualTypeArguments()[ 0 ];
            if( type instanceof Class )
            {
                injectionClass = (Class) type;
            }
            else if( type instanceof ParameterizedType )
            {
                injectionClass = (Class) ( (ParameterizedType) type ).getRawType();
            }
        }
        else if( injectionType instanceof TypeVariable )
        {
            injectionClass = (Class) ( (TypeVariable) injectionType ).getBounds()[ 0 ];
        }
        else
        {
            injectionClass = (Class) injectionType;
        }
    }

    public Class<? extends Annotation> getInjectionAnnotationType()
    {
        return injectionAnnotation;
    }

    public Type getInjectionType()
    {
        return injectionType;
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
    public Class getRawInjectionType()
    {
        return rawInjectionType;
    }

    /**
     * Get the injection class. If the injection uses generics this is the parameter type,
     * and otherwise it is the raw type. Examples:
     *
     * @return
     * @Service MyService service -> MyService
     * @Entity Iterable<Foo> fooList -> Foo
     * @Entity Query<Foo> fooQuery -> Foo
     */
    public Class getInjectionClass()
    {
        return injectionClass;
    }

    public Class getInjectedClass()
    {
        return injectedClass;
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

        InjectionModel that = (InjectionModel) o;

        if( !injectionType.equals( that.injectionType ) )
        {
            return false;
        }

        return true;
    }


    @Override public int hashCode()
    {
        int result;
        result = injectionType.hashCode();
        result = 31 * result + injectedClass.hashCode();
        return result;
    }

    @Override public String toString()
    {
        return ( injectedClass == null ? "" : injectedClass.getSimpleName() + ":" ) + injectionType;
    }

    public boolean isOptional()
    {
        return optional;
    }

    private Class getNonPrimitiveType( Class rawInjectionType )
    {
        if( rawInjectionType.getSimpleName().equals( "boolean" ) )
        {
            return Boolean.class;
        }
        else
        {
            return rawInjectionType;
        }
    }
}