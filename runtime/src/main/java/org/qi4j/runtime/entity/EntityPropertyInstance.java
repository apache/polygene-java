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

import org.qi4j.api.property.PropertyInfo;
import org.qi4j.api.unitofwork.PropertyStateChange;
import org.qi4j.api.unitofwork.StateChangeListener;
import org.qi4j.api.unitofwork.StateChangeVoter;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;

/**
 * {@code EntityPropertyInstance} represents a property whose value should be backed by an EntityState.
 *
 * @author Rickard Öberg
 * @since 0.1.0
 */
public class EntityPropertyInstance<T> extends PropertyInstance<T>
{
    private static final Object NOT_LOADED = new Object();

    private EntityState entityState;
    private UnitOfWorkInstance uow;

    /**
     * Construct an instance of {@code PropertyInstance} with the specified arguments.
     *
     * @param aPropertyInfo The property info. This argument must not be {@code null}.
     * @param entityState
     * @param uow
     * @throws IllegalArgumentException Thrown if the specified {@code aPropertyInfo} is {@code null}.
     * @since 0.1.0
     */
    public EntityPropertyInstance( PropertyInfo aPropertyInfo, EntityState entityState, ConstraintsCheck constraints, UnitOfWorkInstance uow )
        throws IllegalArgumentException
    {
        super( aPropertyInfo, (T) NOT_LOADED, constraints );
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
            value = (T) entityState.getProperty( qualifiedName() );
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
            constraints.checkConstraints( aNewValue );
        }

        // Allow voters to vote on change
        Iterable<StateChangeVoter> stateChangeVoters = uow.stateChangeVoters();
        PropertyStateChange change = null;
        if( stateChangeVoters != null )
        {
            change = new PropertyStateChange( entityState.qualifiedIdentity().identity(), qualifiedName(), aNewValue, get() );

            for( StateChangeVoter stateChangeVoter : stateChangeVoters )
            {
                stateChangeVoter.acceptChange( change );
            }
        }

        Iterable<StateChangeListener> stateChangeListeners = uow.stateChangeListeners();
        if( stateChangeListeners != null )
        {
            // Have to create this here in order to get old value
            change = new PropertyStateChange( entityState.qualifiedIdentity().identity(), qualifiedName(), aNewValue, get() );
        }

        // Change property
        entityState.setProperty( qualifiedName(), aNewValue );
        super.set( aNewValue );

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
}