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

package org.qi4j.runtime.property;

import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.property.AbstractProperty;
import org.qi4j.property.PropertyContainer;
import org.qi4j.property.PropertyVetoException;
import org.qi4j.property.ReadableProperty;
import org.qi4j.property.WritableProperty;
import org.qi4j.spi.property.PropertyBinding;

/**
 * TODO
 */
public final class PropertyContext
{
    private PropertyBinding propertyBinding;

    public PropertyContext( PropertyBinding propertyBinding )
    {
        this.propertyBinding = propertyBinding;
    }

    public PropertyBinding getPropertyBinding()
    {
        return propertyBinding;
    }

    public <T> AbstractProperty newInstance( PropertyContainer<T> container, Object value )
    {
        try
        {
            AbstractProperty instance = propertyBinding.getConstructor().newInstance( container, this, value );

            return instance;
        }
        catch( Exception e )
        {
            throw new InvalidPropertyException( "Could not instantiate property of type " + propertyBinding.getImplementationClass().getName(), e );
        }
    }

    public <T> ReadableProperty<T> getReadableProperty( PropertyInstance<T> propertyInstance )
    {
        PropertyInstanceValue<T> piv = new PropertyInstanceValue<T>();

        ReadableProperty<T> read;

        read = new ReadableProperty<T>()
        {
            @ConcernFor ReadableProperty<T> next;

            public T get()
            {
                System.out.println( "Accessed property" );
                return next.get();
            }
        };
        try
        {
            read.getClass().getDeclaredField( "next" ).set( read, piv );
        }
        catch( IllegalAccessException e )
        {
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }
        catch( NoSuchFieldException e )
        {
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }

        PropertyInvocation<T> pi = new PropertyInvocation<T>( read, piv, piv );
        pi.setPropertyInstance( propertyInstance );
        return pi;
    }

    public <T> WritableProperty<T> getWritableProperty( PropertyInstance<T> propertyInstance )
    {
        PropertyInstanceValue<T> piv = new PropertyInstanceValue<T>();

        WritableProperty<T> writableProperty;

        writableProperty = new WritableProperty<T>()
        {
            @ConcernFor WritableProperty<T> next;

            public void set( T newValue ) throws PropertyVetoException
            {
                System.out.println( "Wrote property" );
                next.set( newValue );
            }
        };
        try
        {
            writableProperty.getClass().getDeclaredField( "next" ).set( writableProperty, piv );
        }
        catch( IllegalAccessException e )
        {
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }
        catch( NoSuchFieldException e )
        {
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }

        PropertyInvocation<T> pi = new PropertyInvocation<T>( piv, writableProperty, piv );
        pi.setPropertyInstance( propertyInstance );
        return pi;
    }
}
