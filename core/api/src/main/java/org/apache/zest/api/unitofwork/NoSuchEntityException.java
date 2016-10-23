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
package org.apache.zest.api.unitofwork;

import java.util.stream.Stream;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.functional.Iterables;

import static java.util.Arrays.stream;

/**
 * This exception indicates that the requested Entity with the given
 * reference does not exist.
 */
public class NoSuchEntityException
    extends UnitOfWorkException
{
    private final EntityReference reference;
    private final Usecase usecase;
    private final Class<?>[] mixinTypes;

    public NoSuchEntityException(EntityReference reference, Class<?> mixinType, Usecase usecase )
    {
        super( "Could not find entity (" + reference + ") of type " + mixinType.getName() + " in usecase '" + usecase.name() + "'" );
        this.reference = reference;
        this.usecase = usecase;
        this.mixinTypes = new Class<?>[]{ mixinType };
    }

    public NoSuchEntityException(EntityReference reference, Class<?>[] mixinTypes, Usecase usecase )
    {
        super( "Could not find entity (" + reference + ") of type " + toString( mixinTypes ) + " in usecase '" + usecase.name() + "'" );
        this.reference = reference;
        this.mixinTypes = mixinTypes;
        this.usecase = usecase;
    }

    public NoSuchEntityException(EntityReference reference, Stream<Class<?>> types, Usecase usecase )
    {
        this(reference, types.toArray( Class[]::new ), usecase );
    }

    public EntityReference reference()
    {
        return reference;
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
        String reduced = stream( mixinTypes ).map( Class::getName ).reduce( "", ( ret, name ) -> ret + "," + name );
        return "[" + reduced.substring( 1 ) + "]";
    }
}
