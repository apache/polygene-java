/*
 * Copyright 2008 Wen Tao. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.library.beans.properties;

import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

class Properties
{
    private Map<String, Object> properties = new HashMap<String, Object>();
    private static final String PREFIX_LIST_NAME = "l:";
    private static final String PREFIX_VALUE_NAME = "v:";

    public Object get( String propertyName )
    {
        String name = encodeValueName( propertyName );
        return properties.get( name );
    }

    public void set( String propertyName, Object value )
    {
        String name = encodeValueName( propertyName );
        if( value == null )
        {
            properties.remove( name );
        }
        else
        {
            properties.put( name, value );
        }
    }

    public void add( String propertyName, Object value )
    {
        String name = encodeListName( propertyName );
        ArrayList list = (ArrayList) properties.get( name );
        if( list == null )
        {
            list = new ArrayList();
            properties.put( name, list );
        }
        list.add( value );
    }

    public void remove( String propertyName, Object value )
    {
        String name = encodeListName( propertyName );
        ArrayList list = (ArrayList) properties.get( name );
        if( list != null )
        {
            list.remove( value );
            if( list.size() == 0 )
            {
                properties.remove( name );
            }
        }
    }

    public Iterator iterator( String propertyName )
    {
        String name = encodeListName( propertyName );
        ArrayList list = (ArrayList) properties.get( name );
        if( list != null )
        {
            return list.iterator();
        }
        return new Iterator()
        {
            public boolean hasNext()
            {
                return false;
            }

            public Object next()
            {
                return null;
            }

            public void remove()
            {
            }
        };
    }

    private String encodeValueName( String propertyName )
    {
        return PREFIX_VALUE_NAME + propertyName;
    }

    private String encodeListName( String propertyName )
    {
        return PREFIX_LIST_NAME + propertyName;
    }

}

