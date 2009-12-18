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
import java.math.BigDecimal;
import java.math.BigInteger;
import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;

/**
 * Number type
 */
public final class NumberType
    extends AbstractValueType
{
    public static boolean isNumber( Type type )
    {
        if( type instanceof Class )
        {
            Class typeClass = (Class) type;
            return ( Number.class.isAssignableFrom( typeClass ) );
        }
        return false;
    }

    public NumberType( TypeName type )
    {
        super( type );
    }

    @Override
    public boolean isNumber()
    {
        return true;
    }

    public void toJSON( Object value, JSONWriter json )
        throws JSONException
    {
        json.value( toJSON( value ) );
    }

    public Object toJSON( Object value )
        throws JSONException
    {
        Number number = (Number) value;
        if( type.isClass( Integer.class ) )
        {
            return number.longValue();
        }
        else if( type.isClass( Long.class ) )
        {
            return number.longValue();
        }
        else if( type.isClass( Double.class ) )
        {
            return number.doubleValue();
        }
        else if( type.isClass( Float.class ) )
        {
            return number.doubleValue();
        }
        else if( type.isClass( Short.class ) )
        {
            return number.longValue();
        }
        else if( type.isClass( BigDecimal.class ) )
        {
            return ( (BigDecimal) number ).toPlainString();
        }
        else if( type.isClass( BigInteger.class ) )
        {
            return number.toString();
        }
        else
        {
            throw new IllegalArgumentException( "Value is not a number:" + value );
        }
    }

    public Object fromJSON( Object json, Module module )
    {
        if( type.isClass( BigDecimal.class ) )
        {
            return new BigDecimal( ( (String) json ) );
        }
        else if( type.isClass( BigInteger.class ) )
        {
            return new BigInteger( (String) json );
        }

        Number number = (Number) json;

        if( type.isClass( Integer.class ) )
        {
            return number.intValue();
        }
        else if( type.isClass( Long.class ) )
        {
            return number.longValue();
        }
        else if( type.isClass( Double.class ) )
        {
            return number.doubleValue();
        }
        else if( type.isClass( Float.class ) )
        {
            return number.floatValue();
        }
        else if( type.isClass( Short.class ) )
        {
            return number.shortValue();
        }

        throw new IllegalStateException( "Unknown number type:" + type );
    }
}