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
package org.qi4j.runtime.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.property.AbstractPropertyInstance;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.PropertyStateChange;
import org.qi4j.api.unitofwork.StateChangeListener;
import org.qi4j.api.unitofwork.StateChangeVoter;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.value.CompoundType;
import org.qi4j.spi.value.SerializableType;
import org.qi4j.spi.value.ValueType;

/**
 * {@code EntityPropertyInstance} represents a property whose value should be backed by an EntityState.
 *
 * @author Rickard Öberg
 * @since 0.1.0
 */
public class EntityPropertyInstance<T> extends AbstractPropertyInstance<T>
{
    private static final Object NOT_LOADED = new Object();

    private EntityState entityState;
    private UnitOfWorkInstance uow;

    private T value;
    private ConstraintsCheck constraints;

    /**
     * Construct an instance of {@code PropertyInstance} with the specified arguments.
     *
     * @param aPropertyInfo The property info. This argument must not be {@code null}.
     * @param entityState
     * @param uow
     * @throws IllegalArgumentException Thrown if the specified {@code aPropertyInfo} is {@code null}.
     * @since 0.1.0
     */
    public EntityPropertyInstance( EntityPropertyModel aPropertyInfo, EntityState entityState, ConstraintsCheck constraints, UnitOfWorkInstance uow )
        throws IllegalArgumentException
    {
        super( aPropertyInfo );
        this.constraints = constraints;
        this.value = (T) NOT_LOADED;
        this.entityState = entityState;
        this.uow = uow;
    }

    /**
     * Returns this property value.
     *
     * @return This property value.
     * @since 0.1.0
     */
    public T get()
    {
        if( value == NOT_LOADED )
        {
            value = ((EntityPropertyModel) propertyInfo ).<T>fromEntityState( uow.module(), entityState );
        }

        return value;
    }

    /**
     * Sets this property value.
     *
     * @param aNewValue The new value.
     */
    public void set( T aNewValue )
    {
        if( isImmutable() )
        {
            throw new IllegalStateException( "Property [" + qualifiedName() + "] is immutable" );
        }

        if( constraints != null )
        {
            constraints.checkConstraints( aNewValue, false );
        }

        // Allow voters to vote on change
        Iterable<StateChangeVoter> stateChangeVoters = uow.stateChangeVoters();
        PropertyStateChange change = null;
        if( stateChangeVoters != null )
        {
            change = new PropertyStateChange( entityState.qualifiedIdentity().identity(), qualifiedName() );

            for( StateChangeVoter stateChangeVoter : stateChangeVoters )
            {
                stateChangeVoter.acceptChange( change );
            }
        }

        Iterable<StateChangeListener> stateChangeListeners = uow.stateChangeListeners();
        if( stateChangeListeners != null )
        {
            // Have to create this here in order to get old value
            change = new PropertyStateChange( entityState.qualifiedIdentity().identity(), qualifiedName() );
        }

        // Change property
        entityState.setProperty( qualifiedName(), ((EntityPropertyModel)propertyInfo).toValue( aNewValue, entityState ) );
        value = aNewValue;

        // Notify listeners
        if( stateChangeListeners != null )
        {
            for( StateChangeListener stateChangeListener : stateChangeListeners )
            {
                stateChangeListener.notify( change );
            }
        }
    }

    /**
     * Returns the value as string.
     *
     * @return The value as string.
     * @since 0.1.0
     */
    @Override
    public String toString()
    {
        Object value = get();
        return value == null ? "" : value.toString();
    }

    public void refresh( EntityState newState )
    {
        value = (T) NOT_LOADED;
        entityState = newState;
    }

    private Object storableValue( ValueType type, Object value )
    {
        if( type instanceof CompoundType )
        {
            Map<String, Object> values = new HashMap<String, Object>();

            ValueComposite valueComposite = (ValueComposite) value;
            ValueInstance instance = ValueInstance.getValueInstance( valueComposite );
            List<PropertyDescriptor> properties = instance.compositeModel().state().properties();
            for( PropertyDescriptor property : properties )
            {

                PropertyTypeDescriptor propertyDescriptor = (PropertyTypeDescriptor) property;
                Property valueProperty = instance.state().getProperty( property.accessor() );
                ValueType compoundPropertyType = propertyDescriptor.propertyType().type();
                Object propertyValue = storableValue( compoundPropertyType, valueProperty.get() );
                values.put( propertyDescriptor.qualifiedName(), propertyValue );
            }
            value = entityState.newValueState( values );
        }
        else if( type instanceof SerializableType )
        {
            // Serialize value
            try
            {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream( bout );
                out.writeObject( value );
                out.close();
                value = bout.toByteArray();
            }
            catch( IOException e )
            {
                throw new IllegalArgumentException( "Could not serialize value", e );
            }
        }

        return value;
    }
}