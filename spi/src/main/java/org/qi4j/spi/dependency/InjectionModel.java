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

package org.qi4j.spi.dependency;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Injection model used as base for all types of injections. The model is comprised
 * of the the annotation, the generic dependency type, and the dependent type that declared the injection.
 */
public class InjectionModel
{
    private Class<? extends Annotation> injectionAnnotation;
    private Type injectedType;
    private Class injectedClass;
    protected boolean optional;

    public InjectionModel( Class<? extends Annotation> injectionAnnotation, Type genericType, Class injectedClass, boolean optional )
    {
        this.injectionAnnotation = injectionAnnotation;
        this.injectedClass = injectedClass;
        this.injectedType = genericType;
        this.optional = optional;
    }

    public Class<? extends Annotation> getInjectionAnnotationType()
    {
        return injectionAnnotation;
    }

    public Type getInjectionType()
    {
        return injectedType;
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
        if( injectedType instanceof Class )
        {
            return (Class) injectedType;
        }
        else if( injectedType instanceof ParameterizedType )
        {
            return (Class) ( (ParameterizedType) injectedType ).getRawType();
        }
        else
        {
            return null; // TODO can this happen?
        }
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
        if( injectedType instanceof ParameterizedType )
        {
            return (Class) ( (ParameterizedType) injectedType ).getActualTypeArguments()[ 0 ];
        }
        else
        {
            return (Class) injectedType;
        }
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

        if( !injectedType.equals( that.injectedType ) )
        {
            return false;
        }

        return true;
    }


    @Override public int hashCode()
    {
        int result;
        result = injectedType.hashCode();
        result = 31 * result + injectedClass.hashCode();
        return result;
    }

    @Override public String toString()
    {
        return ( injectedClass == null ? "" : injectedClass.getSimpleName() + ":" ) + injectedType;
    }

    public boolean isOptional()
    {
        return optional;
    }
}