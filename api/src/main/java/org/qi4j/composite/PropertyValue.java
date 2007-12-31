/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.composite;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * TODO
 */
public class PropertyValue
    implements Serializable
{
    private static ThreadLocal<String> currentName = new ThreadLocal<String>();

    public static PropertyValue property( String name, Object value )
    {
        if( name == null )
        {
            name = currentName.get();
            NullArgumentException.validateNotNull( "name", name );
            currentName.remove();
        }

        return new PropertyValue( name, value );
    }

    public static PropertyValue property( Object nullName, Object value )
    {
        String name = currentName.get();
        if( name == null )
        {
            throw new IllegalStateException( "You must set a name by calling name() first" );
        }
        currentName.remove();

        return new PropertyValue( name, value );
    }

    public static <T> T name( Class<? extends T> mixinType )
    {
        InvocationHandler ih = new PropertyNameInvocationHandler();
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, ih ) );
    }

    private String name;
    private Object value;

    public PropertyValue( String name, Object value )
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        PropertyValue that = (PropertyValue) o;

        if( !name.equals( that.name ) )
        {
            return false;
        }
        if( value != null ? !value.equals( that.value ) : that.value != null )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    @Override public String toString()
    {
        return name + "=" + value;
    }

    static class PropertyNameInvocationHandler
        implements InvocationHandler
    {
        PropertyNameInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            String name = Introspector.decapitalize( method.getName().substring( 3 ) );
            currentName.set( name );

            return method.getDefaultValue();
        }
    }
}