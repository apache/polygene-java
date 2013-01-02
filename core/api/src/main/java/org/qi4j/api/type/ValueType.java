/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.api.type;

import java.util.Collections;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;

import static org.qi4j.functional.Iterables.first;

/**
 * Base class for types of values in ValueComposites and Properties.
 *
 * TODO I think the ValueType system requires some major re-thinking.
 */
public class ValueType
    implements HasTypes
{

    protected final Iterable<Class<?>> types;

    public ValueType( Class<?> type )
    {
        this( Collections.singleton( type ) );
    }

    @SuppressWarnings( "unchecked" )
    public ValueType( Iterable<? extends Class<?>> types )
    {
        this.types = (Iterable<Class<?>>) types;
    }

    public Class<?> mainType()
    {
        return first( types );
    }

    @Override
    public Iterable<Class<?>> types()
    {
        return types;
    }

    @Override
    public String toString()
    {
        String name = Iterables.toString(
            types,
            new Function<Class<?>, String>()
            {
                @Override
                public String map( Class<?> item )
                {
                    return item.getName();
                }
            },
            "," );
        if( name.contains( "," ) )
        {
            name = "{" + name + "}";
        }
        return name;
    }
}