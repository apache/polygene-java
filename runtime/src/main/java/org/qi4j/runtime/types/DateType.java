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

package org.qi4j.runtime.types;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import static org.qi4j.api.common.TypeName.*;
import org.qi4j.api.structure.Module;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Date type. Use ISO8601 format (http://www.w3.org/TR/NOTE-datetime). Assumes UTC time.
 */
public final class DateType
    extends AbstractValueType
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

    public static boolean isDate( Type type )
    {
        if( type instanceof Class )
        {
            Class typeClass = (Class) type;
            return ( typeClass.equals( Date.class ) );
        }
        return false;
    }

    public DateType()
    {
        super( nameOf( Date.class ) );
    }

    @Override
    public boolean isDate()
    {
        return true;
    }

    public void toJSON( Object value, JSONWriter json ) throws JSONException
    {
        Date date = (Date) value;
        String dateString = ISO8601_UTC.get().format( date );
        json.value( dateString );
    }

    public Object fromJSON( Object json, Module module )
    {
        String stringDate = (String) json;

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

    @Override
    public String toQueryParameter( Object value )
        throws IllegalArgumentException
    {
        return value == null ? null : ISO8601_UTC.get().format( (Date) value );
    }

    @Override
    public Object fromQueryParameter( String parameter, Module module )
        throws IllegalArgumentException
    {
        try
        {
            return ISO8601_UTC.get().parse( parameter );
        }
        catch( ParseException e )
        {
            throw new IllegalArgumentException( "Illegal date:" + parameter );
        }
    }
}