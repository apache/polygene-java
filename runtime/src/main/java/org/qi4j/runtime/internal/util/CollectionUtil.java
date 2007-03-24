/*
 * Copyright 2007 Edward Yakop.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.runtime.internal.util;

import java.util.Collection;
import org.ops4j.lang.NullArgumentException;

public final class CollectionUtil
{
    /**
     * Add all entry in {@code array} argument to the specified {@code collection} argument.
     *
     * @param array      The array to be added.
     * @param collection The collection.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code collection} is {@code null}.
     * @since 1.0.0
     */
    public static <T> void addAllToCollection( T[] array, Collection<T> collection )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( collection, "collection" );

        if( array == null )
        {
            return;
        }

        for( T entry : array )
        {
            collection.add( entry );
        }
    }

    /**
     * Returns {@code true} if all the parts belong to the specified {@code array}, {@code false} otherwise.
     *
     * @param parts The parts to check.
     * @param array The array to check against.
     *
     * @return A {@code boolean} value indicates whether any of the parts specified in the specified {@code array}
     *         argument.
     *
     * @since 1.0.0
     */
    public static <T> boolean isAllPartOf( T[] parts, T[] array )
    {
        if( parts == null )
        {
            return true;
        }

        if( array == null )
        {
            return false;
        }
        
        boolean found;
        for( T part : parts )
        {
            found = false;
            for( T entry : array )
            {
                if( equals( entry, part ) )
                {
                    found = true;
                    break;
                }
            }

            if( !found )
            {
                return false;
            }
        }

        return true;
    }

    private static <T> boolean equals( T t1, T t2 )
    {
        if( t1 == t2 )
        {
            return true;
        }

        if( t1 != null )
        {
            return t1.equals( t2 );
        }

        return t2.equals( t1 );
    }

    /**
     * Returns {@code true} if any of the parts belong to the specified {@code array}, {@code false} if the specified
     * {@code parts} argument is {@code null} or none of the item in the {@code parts} argument is specified in
     * {@code array}.
     *
     * @param parts The parts to check.
     * @param array The array to check against. This argument must not be {@code null}.
     *
     * @return A {@code boolean} value indicates whether any of the parts specified in the specified {@code array}
     *         argument.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code array} argument is {@code null}.
     * @since 1.0.0
     */
    public static <T> boolean isAnyPartOf( T[] parts, T[] array )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( array, "array" );

        if( parts == null )
        {
            return false;
        }

        for( T part : parts )
        {
            for( T entry : array )
            {
                if( entry != null )
                {
                    if( entry.equals( part ) )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
