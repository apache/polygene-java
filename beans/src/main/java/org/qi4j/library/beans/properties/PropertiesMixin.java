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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;

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
    private static final PropertyHandler[] HANDLERS = new PropertyHandler[]
        {
            new AbstractPropertyHandler( Setters.SET )
            {
                protected Object handleProperty( Properties properties, String propertyName, Object[] args )
                {
                    properties.set( propertyName, args[ 0 ] );
                    return null;
                }
            },
            new GetPropertyHandler( Getters.GET ),
            new GetPropertyHandler( Getters.IS ),
            new GetPropertyHandler( Getters.HAS ),
            new AbstractPropertyHandler( Setters.ADD )
            {
                public Object handleProperty( Properties properties, String propertyName, Object[] args )
                {
                    properties.add( propertyName, args[ 0 ] );
                    return null;
                }
            },
            new AbstractPropertyHandler( Setters.REMOVE )
            {
                protected Object handleProperty( Properties properties, String propertyName, Object[] args )
                {
                    properties.remove( propertyName, args[ 0 ] );
                    return null;
                }
            },
            new AbstractPropertyHandler( new Iterables() )
            {
                protected Object handleProperty( Properties properties, String propertyName, Object[] args )
                {
                    return properties.iterator( propertyName );
                }
            }
        };

    // Attributes ----------------------------------------------------
    Properties properties;

    /**
     * Construct and empty properties mixins.
     *
     * @since 0.1.0
     */
    public PropertiesMixin()
    {
        properties = new Properties();
    }

    // InvocationHandler implementation ------------------------------
    @SuppressWarnings( "unchecked" )
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        String methodName = method.getName();
        for( PropertyHandler handler : HANDLERS )
        {
            if( handler.shouldHandle( methodName ) )
            {
                return handler.handleInvocation( properties, methodName, args );
            }
        }
        return null;
    }

    private interface PropertyHandler
    {
        boolean shouldHandle( String methodName );

        Object handleInvocation( Properties properties, String methodName, Object[] args );
    }

    private static abstract class AbstractPropertyHandler implements PropertyHandler
    {
        private PropertyNameExtractor propertyNameExtractor;

        public AbstractPropertyHandler( PropertyNameExtractor propertyNameExtractor )
        {
            this.propertyNameExtractor = propertyNameExtractor;
        }

        public Object handleInvocation( Properties properties, String methodName, Object[] args )
        {
            String propertyName = propertyNameExtractor.extractPropertyName( methodName );
            return handleProperty( properties, propertyName, args );
        }

        protected abstract Object handleProperty( Properties properties, String propertyName, Object[] args );

        public boolean shouldHandle( String methodName )
        {
            return propertyNameExtractor.extractPropertyName( methodName ) != null;
        }
    }

    private static final class GetPropertyHandler extends AbstractPropertyHandler
    {
        public GetPropertyHandler( PropertyNameExtractor propertyNameExtractor )
        {
            super( propertyNameExtractor );
        }

        protected Object handleProperty( Properties properties, String propertyName, Object[] args )
        {
            return properties.get( propertyName );
        }
    }
}
