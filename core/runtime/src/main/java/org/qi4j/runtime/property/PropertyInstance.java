/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.PropertyWrapper;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.MapType;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.value.ValueInstance;

/**
 * {@code PropertyInstance} represents a property.
 */
public class PropertyInstance<T>
    implements Property<T>
{
    protected volatile T value;
    protected PropertyInfo model;

    /**
     * Construct an instance of {@code PropertyInstance} with the specified arguments.
     *
     * @param model  The property model. This argument must not be {@code null}.
     * @param aValue The property value.
     */
    public PropertyInstance( PropertyInfo model, T aValue )
    {
        this.model = model;
        value = aValue;
    }

    public PropertyInfo propertyInfo()
    {
        return model;
    }

    /**
     * @param model The property model. This argument must not be {@code null}.
     */
    public void setPropertyInfo( PropertyInfo model )
    {
        this.model = model;
    }

    /**
     * Returns this property value.
     *
     * @return This property value.
     */
    @Override
    public T get()
    {
        return value;
    }

    /**
     * Sets this property value.
     *
     * @param aNewValue The new value.
     */
    @Override
    public void set( T aNewValue )
    {
        if( model.isImmutable() )
        {
            throw new IllegalStateException( "Property [" + model.qualifiedName() + "] is immutable." );
        }

        model.checkConstraints( aNewValue );

        value = aNewValue;
    }

    /**
     * Perform equals with {@code o} argument.
     * <p>
     *     The definition of equals() for the Property is that if both the state and descriptor are equal,
     *     then the properties are equal.
     * </p>
     *
     * @param o The other object to compare.
     * @return Returns a {@code boolean} indicator whether this object is equals the other.
     */
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

        Property<?> that = (Property<?>) o;
        // Unwrap if needed
        while( that instanceof PropertyWrapper )
        {
            that = ( (PropertyWrapper) that ).next();
        }
        // Descriptor equality
        PropertyDescriptor thatDescriptor = (PropertyDescriptor) ( (PropertyInstance) that ).propertyInfo();
        if( !model.equals( thatDescriptor ) )
        {
            return false;
        }
        // State equality
        T value = get();
        if( value == null )
        {
            return that.get() == null;
        }
        return value.equals( that.get() );
    }

    /**
     * Calculate hash code.
     *
     * @return the hashcode of this instance.
     */
    @Override
    public int hashCode()
    {
        int hash = model.hashCode() * 19; // Descriptor
        T value = get();
        if( value != null )
        {
            hash += value.hashCode() * 13; // State
        }
        return hash;
    }

    /**
     * Returns the value as string.
     *
     * @return The value as string.
     */
    @Override
    public String toString()
    {
        Object value = get();
        return value == null ? "" : value.toString();
    }

    @SuppressWarnings( {"raw", "unchecked"} )
    public void prepareToBuild( PropertyModel propertyDescriptor )
    {
        // Check if state has to be modified
        model = propertyDescriptor.getBuilderInfo();
        if( propertyDescriptor.valueType() instanceof ValueCompositeType )
        {
            Object value = get();
            if( value != null )
            {
                ValueInstance.valueInstanceOf( (ValueComposite) value ).prepareToBuild();
            }
        }
        else if( propertyDescriptor.valueType() instanceof CollectionType )
        {
            Object value = get();

            if( value != null )
            {
                if( value instanceof List )
                {
                    value = new ArrayList( (Collection) value );
                }
                else if( value instanceof Set )
                {
                    value = new LinkedHashSet( (Collection) value );
                }

                // Check if items are Values
                CollectionType collection = (CollectionType) propertyDescriptor.valueType();
                if( collection.collectedType() instanceof ValueCompositeType )
                {
                    Collection coll = (Collection) value;
                    for( Object instance : coll )
                    {
                        ValueInstance.valueInstanceOf( (ValueComposite) instance ).prepareToBuild();
                    }
                }

                set( (T) value );
            }
        }
        else if( propertyDescriptor.valueType() instanceof MapType )
        {
            Object value = get();

            if( value != null )
            {
                Map map = new LinkedHashMap( (Map) value );

                // Check if keys/values are Values
                MapType mapType = (MapType) propertyDescriptor.valueType();
                if( mapType.keyType() instanceof ValueCompositeType )
                {
                    for( Object instance : map.keySet() )
                    {
                        ValueInstance.valueInstanceOf( (ValueComposite) instance ).prepareToBuild();
                    }
                }
                if( mapType.valueType() instanceof ValueCompositeType )
                {
                    for( Object instance : map.values() )
                    {
                        ValueInstance.valueInstanceOf( (ValueComposite) instance ).prepareToBuild();
                    }
                }

                set( (T) value );
            }
        }
    }

    @SuppressWarnings( {"raw", "unchecked"} )
    public void prepareBuilderState( PropertyModel propertyDescriptor )
    {
        // Check if state has to be modified
        if( propertyDescriptor.valueType() instanceof ValueCompositeType )
        {
            Object value = get();
            if( value != null )
            {
                ValueInstance.valueInstanceOf( (ValueComposite) value ).prepareBuilderState();
            }
        }
        else if( propertyDescriptor.valueType() instanceof CollectionType )
        {
            T value = get();
            if( value != null )
            {
                if( propertyDescriptor.isImmutable() )
                {
                    if( value instanceof List )
                    {
                        value = (T) Collections.unmodifiableList( (List<? extends Object>) value );
                    }
                    else if( value instanceof Set )
                    {
                        value = (T) Collections.unmodifiableSet( (Set<? extends Object>) value );
                    }
                    else
                    {
                        value = (T) Collections.unmodifiableCollection( (Collection<? extends Object>) value );
                    }

                    this.value = value;
                }

                CollectionType collection = (CollectionType) propertyDescriptor.valueType();
                if( collection.collectedType() instanceof ValueCompositeType )
                {
                    Collection coll = (Collection) value;
                    for( Object instance : coll )
                    {
                        ValueInstance.valueInstanceOf( (ValueComposite) instance ).prepareBuilderState();
                    }
                }
            }
        }
        else if( propertyDescriptor.valueType() instanceof MapType )
        {
            T value = get();

            if( value != null )
            {
                MapType mapType = (MapType) propertyDescriptor.valueType();
                if( mapType.keyType() instanceof ValueCompositeType )
                {
                    Map map = (Map) value;
                    for( Object instance : map.keySet() )
                    {
                        ValueInstance.valueInstanceOf( (ValueComposite) instance ).prepareBuilderState();
                    }
                }
                if( mapType.valueType() instanceof ValueCompositeType )
                {
                    Map map = (Map) value;
                    for( Object instance : map.values() )
                    {
                        ValueInstance.valueInstanceOf( (ValueComposite) instance ).prepareBuilderState();
                    }
                }
                if( propertyDescriptor.isImmutable() )
                {
                    value = (T) Collections.unmodifiableMap( (Map<?, ?>) value );
                }

                this.value = value;
            }
        }

        model = propertyDescriptor;
    }
}
