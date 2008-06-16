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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.property.AbstractPropertyInstance;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.spi.entity.EntityState;
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

    private static final Map<Type, Object> defaultValues;

    static
    {
        defaultValues = new HashMap<Type, Object>();
        defaultValues.put( Integer.class, 0 );
        defaultValues.put( Long.class, 0L );
        defaultValues.put( Double.class, 0.0D );
        defaultValues.put( Float.class, 0.0F );
        defaultValues.put( Boolean.class, false );
    }

    // Better default values for primitives
    private static Object getDefaultValue( Type type )
    {
        return defaultValues.get( type );
    }

    private final String name;
    private final Type type;
    private final Method accessor; // Interface accessor
    private final String qualifiedName;

    private final ValueConstraintsInstance constraints; // May be null
    private final MetaInfo metaInfo;
    private final Object defaultValue;
    private final boolean immutable;

    public PropertyModel( Method anAccessor, ValueConstraintsInstance constraints, MetaInfo metaInfo, Object defaultValue )
    {
        this.metaInfo = metaInfo;
        name = anAccessor.getName();
        type = AbstractPropertyInstance.getPropertyType( anAccessor );
        accessor = anAccessor;
        qualifiedName = AbstractPropertyInstance.getQualifiedName( anAccessor );

        if( defaultValue == null )
        {
            defaultValue = getDefaultValue( type );
        }
        this.defaultValue = defaultValue;

        this.constraints = constraints;

        immutable = ImmutableProperty.class.isAssignableFrom( anAccessor.getReturnType() );
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

    public String toURI()
    {
        return AbstractPropertyInstance.toURI( accessor );
    }

    public void setState( Property property, EntityState entityState )
        throws ConstraintViolationException
    {
        Object value;

        if( property == null || property.get() == ImmutablePropertyInstance.UNSET )
        {
            value = defaultValue;
        }
        else
        {
            value = property.get();
        }

        // Check constraints
        checkConstraints( value );

        entityState.setProperty( qualifiedName, value );
    }

    public Property<?> newInstance()
    {
        return newInstance( defaultValue );
    }

    public Property<?> newInstance( Object value )
    {
        if( immutable )
        {
            return new ImmutablePropertyInstance( this, value );
        }
        else
        {
            return new PropertyInstance<Object>( this, value );
        }
    }

    public void checkConstraints( Object value )
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

    public int hashCode()
    {
        return accessor.hashCode();
    }

    @Override public String toString()
    {
        return accessor.toGenericString();
    }

}
