/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.framework.properties;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.annotation.AppliesTo;

/**
 * Generic property mixin. Methods in interface
 * can be of the following types:
 * setFoo = set property named foo
 * getFoo = get property named foo
 * addFoo = add object to list named foo
 * removeFoo = remove object from list named foo
 */
@AppliesTo( Serializable.class )
public class PropertiesMixin
    implements InvocationHandler
{
    // Attributes ----------------------------------------------------
    Map<String, Object> properties = new HashMap<String, Object>();

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        String methodName = method.getName();
        if( methodName.startsWith( "set" ) )
        {
            // Setter
            String propertyName = methodName.substring( 3 );
            String name = Introspector.decapitalize( propertyName );
            Object value = args[ 0 ];
            Object oldValue;
            if( value == null )
            {
                oldValue = properties.remove( name );
            }
            else
            {
                oldValue = properties.put( name, value );
            }
            if( oldValue instanceof ValueList )
            {
                properties.put( name, oldValue );
                throw new ClassCastException( "Property '" + name + "' is a List. Must use add" + propertyName + "() and remove" + propertyName + "()." );
            }
            return null;
        }
        else if( methodName.startsWith( "get" ) )
        {
            // Getter
            String name = Introspector.decapitalize( methodName.substring( 3 ) );
            return properties.get( name );
        }
        else if( methodName.startsWith( "add" ) )
        {
            // Add to list
            String name = Introspector.decapitalize( methodName.substring( 3 ) );
            List<Object> list = (List<Object>) properties.get( name );
            if( list == null )
            {
                list = new ValueList<Object>();
                properties.put( name, list );
            }
            list.add( args[ 0 ] );
        }
        else if( methodName.startsWith( "remove" ) )
        {
            // Remove from list
            String name = Introspector.decapitalize( methodName.substring( 6 ) );
            Object value = properties.get( name );
            if( value instanceof ValueList )
            {
                ValueList list = (ValueList) value;
                list.remove( args[ 0 ] );
                if( list.size() == 0 )
                {
                    properties.remove( name );
                }
            }
        }
        else if( methodName.endsWith( "Iterator" ) )
        {
            String name = Introspector.decapitalize( methodName.substring( 0, methodName.length() - 8 ) );
            Object value = properties.get( name );
            if( value instanceof ValueList )
            {
                ValueList list = (ValueList) value;
                return list.iterator();
            }
        }
        return null;
    }

    /** Marker class to distinguish a internal list from a assigned List
     * 
     */
    private static class ValueList <T> extends ArrayList<T>
    {
    }
}
