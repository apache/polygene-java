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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.ValueType;

/**
 * Base class for types of values in ValueComposites.
 */
public abstract class AbstractValueType
    implements Serializable, ValueType
{
    private static final List<PropertyType> EMPTY_LIST = new ArrayList<PropertyType>();

    protected final TypeName type;

    protected AbstractValueType( TypeName type )
    {
        this.type = type;
    }

    public TypeName type()
    {
        return type;
    }

    public boolean isNumber()
    {
        return false;
    }

    public boolean isBoolean()
    {
        return false;
    }

    public boolean isString()
    {
        return false;
    }

    public boolean isDate()
    {
        return false;
    }

    public boolean isValue()
    {
        return false;
    }

    public boolean isEnum()
    {
        return false;
    }

    public List<PropertyType> types()
    {
        return EMPTY_LIST;
    }

    public abstract void toJSON( Object value, JSONWriter json )
        throws JSONException;

    public abstract Object toJSON( Object value )
        throws JSONException;

    public abstract Object fromJSON( Object object, Module module )
        throws JSONException;

    public String toQueryParameter( Object value )
        throws IllegalArgumentException
    {
        if( value == null )
        {
            return null;
        }

        JSONStringer jsonStringer = new JSONStringer();
        try
        {
            jsonStringer.array();
            toJSON( value, jsonStringer );
            jsonStringer.endArray();
        }
        catch( JSONException e )
        {
            throw new IllegalArgumentException( "Query parameter value is not a proper JSON value", e );
        }
        String str = jsonStringer.toString();
        return str.substring( 1, str.length() - 1 );
    }

    public Object fromQueryParameter( String parameter, Module module )
        throws IllegalArgumentException, JSONException
    {
        if( parameter == null )
        {
            return null;
        }

        return fromJSON( new JSONTokener( parameter ).nextValue(), module );
    }

    @Override
    public String toString()
    {
        return type.toString();
    }
}