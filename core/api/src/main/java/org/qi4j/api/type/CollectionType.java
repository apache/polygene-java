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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.qi4j.api.util.Classes;

/**
 * Collection ValueType.
 * <p>This handles Collection, List and Set types.</p>
 */
public final class CollectionType
    extends ValueType
{

    public static boolean isCollection( Type type )
    {
        Class<?> cl = Classes.RAW_CLASS.map( type );
        return cl.equals( Collection.class ) || cl.equals( List.class ) || cl.equals( Set.class );
    }

    public static CollectionType collectionOf( Class<?> collectedType )
    {
        return new CollectionType( Collection.class, ValueType.of( collectedType ) );
    }

    public static CollectionType listOf( Class<?> collectedType )
    {
        return new CollectionType( List.class, ValueType.of( collectedType ) );
    }

    public static CollectionType setOf( Class<?> collectedType )
    {
        return new CollectionType( Set.class, ValueType.of( collectedType ) );
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

    @Override
    public String toString()
    {
        return super.toString() + "<" + collectedType + ">";
    }
}
