/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.spi.property;

import org.qi4j.property.Property;
import org.qi4j.property.PropertyInfo;
import org.qi4j.property.PropertyVetoException;

public abstract class ComputedPropertyInstance<T>
    implements Property<T>
{
    protected PropertyInfo propertyInfo;

    public ComputedPropertyInstance( PropertyInfo propertyInfo )
    {
        this.propertyInfo = propertyInfo;
    }

    public T get()
    {
        return null;
    }

    public void set( T newValue )
        throws PropertyVetoException
    {
        throw new PropertyVetoException( "Property '" + getQualifiedName() + "' is read-only" );
    }

    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return propertyInfo.getPropertyInfo( infoType );
    }

    public String getName()
    {
        return propertyInfo.getName();
    }

    public String getQualifiedName()
    {
        return propertyInfo.getQualifiedName();
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

        ComputedPropertyInstance that = (ComputedPropertyInstance) o;

        if( propertyInfo != null ? !propertyInfo.equals( that.propertyInfo ) : that.propertyInfo != null )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return ( propertyInfo != null ? propertyInfo.hashCode() : 0 );
    }
}
