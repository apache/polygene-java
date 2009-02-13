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

package org.qi4j.spi.property;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * TODO
 */
public class CollectionType
    implements ValueType
{
    public static boolean isCollection( Type type)
    {
        if (type instanceof ParameterizedType )
        {
            ParameterizedType pt = (ParameterizedType) type;
            if (pt.getRawType().equals( Collection.class) || pt.getRawType().equals( List.class) || pt.getRawType().equals( Set.class))
                return true;
        }

        return false;
    }

    private String collectionType;
    private ValueType type;

    public CollectionType( String collectionType, ValueType type )
    {
        this.collectionType = collectionType;
        this.type = type;
    }

    public String collectionType()
    {
        return collectionType;
    }

    public ValueType type()
    {
        return type;
    }
}
