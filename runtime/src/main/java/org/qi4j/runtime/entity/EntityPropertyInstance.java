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

import org.qi4j.api.property.AbstractPropertyInstance;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;

/**
 * {@code EntityPropertyInstance} represents a property whose value must be backed by an EntityState.
 *
 * @author Rickard Öberg
 * @since 0.1.0
 */
public class EntityPropertyInstance<T> extends AbstractPropertyInstance<T>
{
    private static final Object NOT_LOADED = new Object();

    private EntityState entityState;
    private ModuleUnitOfWork uow;

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
    public EntityPropertyInstance( EntityPropertyModel aPropertyInfo, EntityState entityState, ConstraintsCheck constraints, ModuleUnitOfWork uow )
        throws IllegalArgumentException
    {
        super( aPropertyInfo );

        validateNotNull( "entitystate", entityState );

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
            value = ( (EntityPropertyModel) propertyInfo ).<T>fromEntityState( uow.module(), entityState );
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

        // Change property
        Qi4jSPI spi = uow.module().layerInstance().applicationInstance().runtime();
        String json = ( (EntityPropertyModel) propertyInfo ).toJSON( aNewValue, spi );
        entityState.setProperty( ( (EntityPropertyModel) propertyInfo ).propertyType().stateName(), json );
        value = aNewValue;
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

    public void refresh()
    {
        value = (T) NOT_LOADED;
    }
}