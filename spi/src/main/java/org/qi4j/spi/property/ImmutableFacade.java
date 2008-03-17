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

import java.lang.reflect.Type;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyVetoException;

public class ImmutableFacade<T>
    implements ImmutableProperty<T>
{
    private final Property<T> target;

    public ImmutableFacade( Property<T> target )
    {
        NullArgumentException.validateNotNull( "target", target );
        this.target = target;
    }

    public T get()
    {
        return target.get();
    }

    public T set( T newValue )
        throws PropertyVetoException
    {
        throw new PropertyVetoException( "Property '" + getQualifiedName() + "' is immutable." );
    }

    // PropertyInfo
    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return target.getPropertyInfo( infoType );
    }

    public String getName()
    {
        return target.getName();
    }

    public String getQualifiedName()
    {
        return target.getQualifiedName();
    }

    public Type getPropertyType()
    {
        return target.getPropertyType();
    }

    @Override public String toString()
    {
        return "[" + target.toString() + ",r/o]";
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

        ImmutableFacade that = (ImmutableFacade) o;

        if( !target.equals( that.target ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return target.hashCode();
    }
}
