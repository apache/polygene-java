/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007-2008, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007, Alin Dreghiciu. All Rights Reserved. 
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
package org.qi4j.api.unitofwork;

import org.qi4j.functional.Function;

import static org.qi4j.functional.Iterables.fold;

/**
 * Qi4j exception to be thrown in case that an entity composite
 * was not found during a lookup call.
 */
public class EntityTypeNotFoundException
    extends UnitOfWorkException
{
    private final String compositeType;

    public EntityTypeNotFoundException( String entityType, String moduleName, Iterable<String> visibility )
    {
        super( "Could not find an EntityComposite of type " + entityType + " in module [" + moduleName + "].\n" +
               "\tThe following entity types are visible:\n" + join(visibility) );
        this.compositeType = entityType;
    }

    private static String join( Iterable<String> visibility )
    {
        return fold( new Function<String, String>()
        {
            StringBuilder result;
            {
                result = new StringBuilder();
            }

            @Override
            public String map( String type )
            {
                result.append( type );
                result.append( "\n" );
                return result.toString();
            }
        }, visibility );
    }

    public String compositeType()
    {
        return compositeType;
    }
}
