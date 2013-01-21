/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.qi4j.api.query;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.util.Classes;

/**
 * Thrown in case that a non queryable type or accessor (marked with @Queriable(false)) is used during query building,
 * or when non-Property, non-Associations are trying to be queried (possibly can not happen).
 */
public class NotQueryableException
    extends QueryException
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param message exception message
     */
    public NotQueryableException( final String message )
    {
        super( message );
    }

    /**
     * Verify that the provided accessor method has not been marked with a Queryable(false).
     *
     * @param accessor accessor method
     *
     * @throws NotQueryableException - If accessor method has been marked as not queryable
     */
    public static void throwIfNotQueryable( final AccessibleObject accessor )
    {
        Queryable queryable = accessor.getAnnotation( Queryable.class );
        if( queryable != null && !queryable.value() )
        {
            throw new NotQueryableException(
                String.format(
                    "%1$s \"%2$s\" (%3$s) is not queryable as has been marked with @Queryable(false)",
                    Classes.RAW_CLASS.map( GenericPropertyInfo.propertyTypeOf( accessor ) ).getSimpleName(),
                    ( (Member) accessor ).getName(),
                    ( (Member) accessor ).getDeclaringClass().getName()
                )
            );
        }
    }

    /**
     * Verify that the provided type has not been marked with a Queryable(false).
     *
     * @param type a type
     *
     * @throws NotQueryableException - If type has been marked as not queryable
     */
    public static void throwIfNotQueryable( final Class<?> type )
    {
        Queryable queryable = type.getAnnotation( Queryable.class );
        if( queryable != null && !queryable.value() )
        {
            throw new NotQueryableException(
                String.format(
                    "Type \"%1$s\" is not queryable as has been marked with @Queryable(false)",
                    type.getName()
                )
            );
        }
    }
}