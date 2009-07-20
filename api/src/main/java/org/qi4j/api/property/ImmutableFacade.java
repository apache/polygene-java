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
package org.qi4j.api.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.util.NullArgumentException;

/**
 * Use the ImmutableFacade if you have a Property that you want to
 * expose as an immutable Property.
 */
public final class ImmutableFacade<T>
    implements Property<T>
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

    public void set( T newValue )
        throws IllegalArgumentException
    {
        throw new IllegalArgumentException( "Property '" + qualifiedName() + "' is immutable." );
    }

    // I think that using T again here is a mistake...
    public <V> V metaInfo( Class<V> infoType )
    {
        if( infoType.equals( Immutable.class ) )
        {
            return infoType.cast( new Immutable()
            {
                public Class<? extends Annotation> annotationType()
                {
                    return Immutable.class;
                }
            } );
        }

        return target.metaInfo( infoType );
    }

    public QualifiedName qualifiedName()
    {
        return target.qualifiedName();
    }

    public Type type()
    {
        return target.type();
    }

    public boolean isImmutable()
    {
        return true;
    }

    public boolean isComputed()
    {
        return false;
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

        ImmutableFacade<?> that = (ImmutableFacade<?>) o;

        return target.equals( that.target );

    }

    public int hashCode()
    {
        return target.hashCode();
    }
}
