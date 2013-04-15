/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.api.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Useful methods for handling Dates.
 */
public final class Dates
{
    // Formatters are not thread-safe. Create one per thread
    private static ThreadLocal<DateFormat> ISO8601 = new ThreadLocal<DateFormat>()
    {
        @Override
        protected DateFormat initialValue()
        {
            return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
        }
    };

    private static ThreadLocal<DateFormat> ISO8601_UTC = new ThreadLocal<DateFormat>()
    {
        @Override
        protected DateFormat initialValue()
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
            dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            return dateFormat;
        }
    };

    /**
     * @param stringDate a string representing a date as either ISO8601, @millis@ or /Date() formats
     * @return a Date
     */
    public static Date fromString( String stringDate )
    {
        try
        {
            Date date = ISO8601_UTC.get().parse( stringDate );
            return date;
        }
        catch( ParseException e )
        {
            try
            {
                Date date = ISO8601.get().parse( stringDate );
                return date;
            }
            catch( ParseException e1 )
            {
                // @millis@ format
                if( stringDate.startsWith( "@" ) && stringDate.endsWith( "@" ) )
                {
                    long time = Long.parseLong( stringDate.substring( 1, stringDate.length() - 1 ) );
                    Date date = new Date( time );
                    return date;
                }
                else if( stringDate.startsWith( "/Date(" ) && stringDate.endsWith( ")/" ) ) // Microsoft format
                {
                    long time = Long.parseLong( stringDate.substring( 6, stringDate.length() - 2 ) );
                    Date date = new Date( time );
                    return date;
                }
                throw new IllegalStateException( "Illegal date:" + stringDate );
            }
        }
    }

    /**
     * @param date a Date
     * @return String representation in ISO8601 UTC
     */
    public static String toUtcString( Date date )
    {
        return ISO8601_UTC.get().format( date );
    }
}
