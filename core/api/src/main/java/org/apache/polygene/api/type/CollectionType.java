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
package org.apache.polygene.api.type;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.polygene.api.util.Classes;

/**
 * Collection ValueType.
 * <p>This handles Collection, List and Set types.</p>
 */
public final class CollectionType
    extends ValueType
{
    public static boolean isCollection( Type type )
    {
        Class<?> cl = Classes.RAW_CLASS.apply( type );
        return Collection.class.isAssignableFrom( cl );
    }

    public static CollectionType of( Class<?> type, ValueType collectedType )
    {
        return new CollectionType( type, collectedType );
    }

    public static CollectionType of( Class<?> type, Class<?> collectedType )
    {
        return of( type, ValueType.of( collectedType ) );
    }

    public static CollectionType collectionOf( ValueType collectedType )
    {
        return of( Collection.class, collectedType );
    }

    public static CollectionType collectionOf( Class<?> collectedType )
    {
        return of( Collection.class, collectedType );
    }

    public static CollectionType listOf( ValueType collectedType )
    {
        return of( List.class, collectedType );
    }

    public static CollectionType listOf( Class<?> collectedType )
    {
        return of( List.class, collectedType );
    }

    public static CollectionType setOf( ValueType collectedType )
    {
        return of( Set.class, collectedType );
    }

    public static CollectionType setOf( Class<?> collectedType )
    {
        return of( Set.class, collectedType );
    }

    private ValueType collectedType;

    public CollectionType( Class<?> type, ValueType collectedType )
    {
        super( type );
        this.collectedType = collectedType;
        if( !isCollection( type ) )
        {
            throw new IllegalArgumentException( type + " is not a Collection, List or Set." );
        }
    }

    public ValueType collectedType()
    {
        return collectedType;
    }

    public boolean isSet()
    {
        return Set.class.isAssignableFrom( primaryType() );
    }

    public boolean isList()
    {
        return List.class.isAssignableFrom( primaryType() );
    }

    @Override
    public boolean equals( final Object o )
    {
        if( this == o ) { return true; }
        if( o == null || getClass() != o.getClass() ) { return false; }
        if( !super.equals( o ) ) { return false; }
        CollectionType that = (CollectionType) o;
        return Objects.equals( collectedType, that.collectedType );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( super.hashCode(), collectedType );
    }

    @Override
    public String toString()
    {
        return super.toString() + "<" + collectedType + ">";
    }
}
