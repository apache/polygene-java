/*
 * Copyright 2007 Rickard Öberg. All Rights Reserved.
 * Copyright 2007 Alin Dreghiciu. All Rights Reserved.
 * Copyright 2007 Edward Yakop. All Rights Reserved.
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

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import org.qi4j.composite.AppliesTo;

/**
 * Generic property mixin. Methods in interface
 * can be of the following types:
 * setFoo = set property named foo
 * getFoo = get property named foo
 * addFoo = add object to list named foo
 * removeFoo = remove object from list named foo
 * fooIterator - return an iterator over the list of Foos
 */
@AppliesTo( { Getters.class, Setters.class, Iterables.class } )
public class PropertiesMixin implements InvocationHandler
{
    private static final PropertyHandler[] HANDLERS = new PropertyHandler[]{
        new SetValue(),
        new GetValue( Getters.GET ),
        new GetValue( Getters.IS ),
        new GetValue( Getters.HAS ),
        new AddToList(),
        new RemoveFromList(),
        new GetIteratorFromList()
    };

    // Attributes ----------------------------------------------------
    Map<String, Object> properties;

    /**
     * Construct and empty properties mixins.
     *
     * @since 0.1.0
     */
    public PropertiesMixin()
    {
        properties = new HashMap<String, Object>();
    }

    // InvocationHandler implementation ------------------------------
    @SuppressWarnings( "unchecked" )
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        String methodName = method.getName();
        Object arg = GetFirstArg( args );
        for( PropertyHandler handler : HANDLERS )
        {
            if( handler.matches( methodName ) )
            {
                return handler.handle( properties, methodName, arg );
            }
        }
        return null;
    }

    private Object GetFirstArg( Object[] args )
    {
        Object arg = null;
        if( args != null )
        {
            arg = args[ 0 ];
        }
        return arg;
    }

    private interface PropertyHandler extends MethodNameFilter
    {
        Object handle( Map<String, Object> properties, String methodName, Object arg );
    }

    private static abstract class AbstractPropertyHandler implements PropertyHandler
    {
        private MethodNameFilter filter;

        public AbstractPropertyHandler( MethodNameFilter filter )
        {
            this.filter = filter;
        }

        public boolean matches( String methodName )
        {
            return filter.matches( methodName );
        }
    }

    private static abstract class PrefixBasedPropertyHandler extends AbstractPropertyHandler
    {
        private int prefixLength;

        public PrefixBasedPropertyHandler( MethodNamePrefixAppliesToFilter filter )
        {
            super( filter );
            String prefix = filter.getPrefix();
            prefixLength = prefix.length();
        }

        protected String getPropertyName( String methodName )
        {
            return Introspector.decapitalize( methodName.substring( prefixLength ) );
        }
    }

    private static final class GetValue extends PrefixBasedPropertyHandler
    {
        public GetValue( MethodNamePrefixAppliesToFilter filter )
        {
            super( filter );
        }

        public Object handle( Map<String, Object> properties, String methodName, Object arg )
        {
            String name = "v:" + getPropertyName( methodName );
            return properties.get( name );
        }
    }

    private static final class SetValue extends PrefixBasedPropertyHandler
    {
        public SetValue()
        {
            super( Setters.SET );
        }

        public Object handle( Map<String, Object> properties, String methodName, Object arg )
        {
            String name = "v:" + getPropertyName( methodName );
            if( arg == null )
            {
                properties.remove( name );
            }
            else
            {
                properties.put( name, arg );
            }
            return null;
        }
    }

    private static final class AddToList extends PrefixBasedPropertyHandler
    {
        public AddToList()
        {
            super( Setters.ADD );
        }

        public Object handle( Map<String, Object> properties, String methodName, Object arg )
        {
            String name = "l:" + getPropertyName( methodName );
            ArrayList<Object> list = (ArrayList<Object>) properties.get( name );
            if( list == null )
            {
                list = new ArrayList<Object>();
                properties.put( name, list );
            }
            list.add( arg );
            return null;
        }
    }

    private static final class RemoveFromList extends PrefixBasedPropertyHandler
    {
        public RemoveFromList()
        {
            super( Setters.REMOVE );
        }

        public Object handle( Map<String, Object> properties, String methodName, Object arg )
        {
            String name = "l:" + getPropertyName( methodName );
            ArrayList<Object> list = (ArrayList<Object>) properties.get( name );
            if( list != null )
            {
                list.remove( arg );
                if( list.size() == 0 )
                {
                    properties.remove( name );
                }
            }
            return null;
        }
    }

    private static final class GetIteratorFromList extends AbstractPropertyHandler
    {
        public GetIteratorFromList()
        {
            super( new Iterables() );
        }

        public Object handle( Map<String, Object> properties, String methodName, Object arg )
        {
            String name = "l:" + Introspector.decapitalize( methodName.substring( 0, methodName.length() - 8 ) );
            ArrayList<Object> list = (ArrayList<Object>) properties.get( name );
            if( list != null )
            {
                return list.iterator();
            }
            return new DummyIterator();
        }

        private static final class DummyIterator implements Iterator
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
        }
    }
}
