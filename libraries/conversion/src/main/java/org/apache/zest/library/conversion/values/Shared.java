/*
 * Copyright (c) 2014-2015 Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.conversion.values;

import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.type.MapType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.functional.Specification;

/**
 * Shared.
 */
final class Shared
{
    static final Specification<ValueType> STRING_TYPE_SPEC;
    static final Specification<ValueType> STRING_COLLECTION_TYPE_SPEC;
    static final Specification<ValueType> STRING_MAP_TYPE_SPEC;

    static
    {
        // Type Specifications
        STRING_TYPE_SPEC = new Specification<ValueType>()
        {
            @Override
            public boolean satisfiedBy( ValueType valueType )
            {
                return valueType.mainType().equals( String.class );
            }
        };
        STRING_COLLECTION_TYPE_SPEC = new Specification<ValueType>()
        {
            @Override
            public boolean satisfiedBy( ValueType valueType )
            {
                return valueType instanceof CollectionType
                       && ( (CollectionType) valueType ).collectedType().mainType().equals( String.class );
            }
        };
        STRING_MAP_TYPE_SPEC = new Specification<ValueType>()
        {
            @Override
            public boolean satisfiedBy( ValueType valueType )
            {
                return valueType instanceof MapType
                       && ( (MapType) valueType ).keyType().mainType().equals( String.class )
                       && ( (MapType) valueType ).valueType().mainType().equals( String.class );
            }
        };
    }

    private Shared()
    {
    }
}
