/*
 * Copyright (c) 2008, Michael Hunger. All Rights Reserved.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default values for various property types
 */
public final class DefaultValues
{
    private static final Map<Type, Object> defaultValues = createDefaultValues();

    private static Map<Type, Object> createDefaultValues()
    {
        Map<Type, Object> defaultValues = new HashMap<Type, Object>();
        defaultValues.put( Byte.class, 0 );
        defaultValues.put( Short.class, 0 );
        defaultValues.put( Character.class, 0 );
        defaultValues.put( Integer.class, 0 );
        defaultValues.put( Long.class, 0L );
        defaultValues.put( Double.class, 0D );
        defaultValues.put( Float.class, 0F );
        defaultValues.put( Boolean.class, false );
        defaultValues.put( String.class, "" );
        return defaultValues;
    }

    public static Object getDefaultValue( Type type )
    {
        Object value = defaultValues.get( type );
        if( value != null )
        {
            return value;
        }
        if( type instanceof ParameterizedType )
        {
            // List<Foo> -> List
            type = ( (ParameterizedType) type ).getRawType();
        }

        if( type instanceof Class )
        {
            Class typeAsClass = (Class) type;
            if( Set.class.isAssignableFrom( typeAsClass ) )
            {
                return new HashSet();
            }
            else if( Map.class.isAssignableFrom( typeAsClass ) )
            {
                return new LinkedHashMap();
            }
            else if( Collection.class.isAssignableFrom( typeAsClass ) )
            {
                return new ArrayList();
            }
            else if( typeAsClass.isEnum() )
            {
                return ( (Class) type ).getEnumConstants()[ 0 ];
            }
        }
        throw new IllegalArgumentException( "Cannot use @UseDefaults with type " + type.toString() );
    }
}
