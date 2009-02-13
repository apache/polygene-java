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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Primitive type
 */
public class PrimitiveType
    implements ValueType
{
    private static Set<Class> primitiveClasses = new HashSet<Class>();

    static
    {
        primitiveClasses.add( Long.class );
        primitiveClasses.add( Integer.class );
        primitiveClasses.add( Float.class );
        primitiveClasses.add( Double.class );
        primitiveClasses.add( Character.class );
        primitiveClasses.add( Boolean.class );
        primitiveClasses.add( Short.class );
        primitiveClasses.add( Byte.class );
        primitiveClasses.add( String.class );
    }

    public static boolean isPrimitive( Type type)
    {
        if (type instanceof Class)
        {
            Class typeClass = (Class) type;
            if (typeClass.isPrimitive())
                return true;
        }

        return primitiveClasses.contains( type );
    }

    private String type;

    public PrimitiveType( String type )
    {
        this.type = type;
    }

    public String type()
    {
        return type;
    }

    @Override public String toString()
    {
        return type;
    }
}
