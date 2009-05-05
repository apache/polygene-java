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

package org.qi4j.spi.value;

import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.util.PeekableStringTokenizer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date type. Use ISO8601 format (http://www.w3.org/TR/NOTE-datetime). Assumes UTC time.
 */
public class DateType
    extends ValueType
{
    // Formatters are not thread-safe. Create one per thread
    private static ThreadLocal<DateFormat> ISO8601 = new ThreadLocal<DateFormat>()
    {
        @Override
        protected DateFormat initialValue()
        {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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

    public DateType( TypeName type )
    {
        super(type);
    }

    public void toJSON( Object value, StringBuilder json )
    {
        json.append( '"' );
        Date date = (Date) value;
        String dateString = ISO8601.get().format(date);
        json.append(dateString);
        json.append( '"' );
    }

    public Object fromJSON( PeekableStringTokenizer json, Module module )
    {
        String token = json.nextToken( "\"" );
        String result = json.nextToken();

        try
        {
            Date date = ISO8601.get().parse(result);
            token = json.nextToken();
            return date;
        } catch (ParseException e)
        {
            throw new IllegalStateException("Illegal date:"+result);
        }
    }

    @Override public String toQueryParameter( Object value )
            throws IllegalArgumentException
    {
        return value == null ? null : ISO8601.get().format((Date) value);
    }

    @Override public Object fromQueryParameter( String parameter, Module module )
            throws IllegalArgumentException
    {
        try
        {
            return ISO8601.get().parse(parameter);
        } catch (ParseException e)
        {
            throw new IllegalArgumentException("Illegal date:"+parameter);
        }
    }
}