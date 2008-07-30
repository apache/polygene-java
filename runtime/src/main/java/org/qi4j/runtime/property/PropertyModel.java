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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.property.AbstractPropertyInstance;
import static org.qi4j.property.AbstractPropertyInstance.getPropertyType;
import static org.qi4j.property.AbstractPropertyInstance.getQualifiedName;
import org.qi4j.property.ComputedProperty;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyInfo;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public class PropertyModel
    implements Serializable, PropertyDescriptor
{
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Type type;
    private final Method accessor; // Interface accessor
    private final String qualifiedName;

    private final ValueConstraintsInstance constraints; // May be null
    private final MetaInfo metaInfo;
    private final Object defaultValue;
    private final boolean immutable;
    private final boolean computed;

    public PropertyModel(
        Method anAccessor, ValueConstraintsInstance constraints, MetaInfo aMetaInfo, Object aPropertyDefaultValue )
    {
        metaInfo = aMetaInfo;
        name = anAccessor.getName();
        type = getPropertyType( anAccessor );
        accessor = anAccessor;
        qualifiedName = getQualifiedName( anAccessor );
        defaultValue = aPropertyDefaultValue;

        this.constraints = constraints;

        immutable = ImmutableProperty.class.isAssignableFrom( anAccessor.getReturnType() );
        computed = ComputedProperty.class.isAssignableFrom( anAccessor.getReturnType() );
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public String name()
    {
        return name;
    }

    public String qualifiedName()
    {
        return qualifiedName;
    }

    public Type type()
    {
        return type;
    }

    public Method accessor()
    {
        return accessor;
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public boolean isComputed()
    {
        return computed;
    }

    public Object defaultValue()
    {
        return defaultValue;
    }

    public String toURI()
    {
        return AbstractPropertyInstance.toURI( accessor );
    }

    public Property<?> newInstance()
    {
        return newInstance( ImmutablePropertyInstance.UNSET );
    }

    @SuppressWarnings( "unchecked" )
    public Property<?> newInstance( Object value )
    {
        if( immutable )
        {
            return new ImmutablePropertyInstance( this, value );
        }
        else if( computed )
        {
            return new ComputedPropertyInfo( this );
        }
        else
        {
            return new PropertyInstance<Object>( this, value );
        }
    }

    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( accessor, violations );
            }
        }
    }

    @Override
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

        PropertyModel that = (PropertyModel) o;

        if( !accessor.equals( that.accessor ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return accessor.hashCode();
    }

    @Override
    public String toString()
    {
        return accessor.toGenericString();
    }

    private static class ComputedPropertyInfo
        extends ComputedPropertyInstance
    {
        private ComputedPropertyInfo( PropertyInfo aPropertyInfo )
            throws IllegalArgumentException
        {
            super( aPropertyInfo );
        }

        public Object get()
        {
            throw new IllegalStateException( "Property [" + name() + "] must be computed" );
        }
    }

}
