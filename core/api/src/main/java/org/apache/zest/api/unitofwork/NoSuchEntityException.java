/*
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
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
package org.apache.zest.api.unitofwork;

import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.functional.Function;
import org.apache.zest.functional.Iterables;

/**
 * This exception indicates that the requested Entity with the given
 * identity does not exist.
 */
public class NoSuchEntityException
    extends UnitOfWorkException
{
    private final EntityReference identity;
    private final Usecase usecase;
    private final Class<?>[] mixinTypes;

    public NoSuchEntityException( EntityReference identity, Class<?> mixinType, Usecase usecase )
    {
        super( "Could not find entity (" + identity + ") of type " + mixinType.getName() + " in usecase '" + usecase.name() + "'" );
        this.identity = identity;
        this.usecase = usecase;
        this.mixinTypes = new Class<?>[]{ mixinType };
    }

    public NoSuchEntityException( EntityReference identity, Class<?>[] mixinTypes, Usecase usecase )
    {
        super( "Could not find entity (" + identity + ") of type " + toString( mixinTypes ) + " in usecase '" + usecase.name() + "'" );
        this.identity = identity;
        this.mixinTypes = mixinTypes;
        this.usecase = usecase;
    }

    public NoSuchEntityException( EntityReference identity, Iterable<Class<?>> types, Usecase usecase )
    {
        this( identity, castToArray( types ), usecase );
    }

    public EntityReference identity()
    {
        return identity;
    }

    public Class<?>[] mixinTypes()
    {
        return mixinTypes;
    }

    public Usecase usecase()
    {
        return usecase;
    }

    private static Class<?>[] castToArray( Iterable<Class<?>> iterableClasses )
    {
        Iterable<Class> types = Iterables.cast( iterableClasses );
        return Iterables.toArray( Class.class, types );
    }

    private static String toString( Class<?>[] mixinTypes )
    {
        Iterable<String> map = Iterables.map( new Function<Class<?>, String>()
        {
            @Override
            public String map( Class<?> item )
            {
                return item.getName();
            }
        }, Iterables.iterable( mixinTypes ) );
        return Iterables.fold( new Function<String, String>()
        {
            StringBuilder result;
            boolean first = true;

            {
                result = new StringBuilder();
                result.append( "[" );
            }

            @Override
            public String map( String strings )
            {
                if( !first )
                {
                    result.append( ',' );
                }
                first = false;
                result.append( strings );
                return result.toString() + "]";
            }
        }, map );
    }
}
