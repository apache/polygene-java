/*
 * Copyright (c) 2014-2015 Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.conversion.values;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.functional.Function;

/**
 * Create or update Entities from matching Values.
 * @deprecated Please use {@link org.qi4j.api.unitofwork.UnitOfWork#toEntity(Class, Identity)} instead.
 */
public interface ValueToEntity
{
    /**
     * Create an Entity from a Value.
     * <p>
     * If the Value extends {@link Identity} the Entity identity is taken from the Value's state.
     * Else, if the Value's state for {@code Identity} is absent, a new Identity is generated.
     *
     * @param <T>        Value Type
     * @param entityType Entity Type
     * @param value      Value
     *
     * @return the created Entity
     */
    <T> T create( Class<T> entityType, Object value );

    /**
     * Create an Entity from a Value.
     * <p>
     * If {@code identity} is not null, it is used as Entity identity.
     * Else, if the Value extends {@link Identity} the Entity identity is taken from the Value's state.
     * Else, if the Value's state for {@code Identity} is absent, a new Identity is generated.
     *
     * @param <T>        Value Type
     * @param entityType Entity Type
     * @param identity   Entity Identity, may be null
     * @param value      Value
     *
     * @return the created Entity
     */
    <T> T create( Class<T> entityType, String identity, Object value );

    /**
     * Create an Entity from a Value.
     * <p>
     * If the Value extends {@link Identity} the Entity identity is taken from the Value's state.
     * Else, if the Value's state for {@code Identity} is absent, a new Identity is generated.
     *
     * @param <T>                  Value Type
     * @param entityType           Entity Type
     * @param value                Value
     * @param prototypeOpportunity A Function that will be mapped on the Entity prototype before instanciation
     *
     * @return the created Entity
     */
    <T> T create( Class<T> entityType, Object value, Function<T, T> prototypeOpportunity );

    /**
     * Create an Entity from a Value.
     * <p>
     * If {@code identity} is not null, it is used as Entity identity.
     * Else, if the Value extends {@link Identity} the Entity identity is taken from the Value's state.
     * Else, if the Value's state for {@code Identity} is absent, a new Identity is generated.
     *
     * @param <T>                  Value Type
     * @param entityType           Entity Type
     * @param identity             Entity Identity, may be null
     * @param value                Value
     * @param prototypeOpportunity A Function that will be mapped on the Entity prototype before instanciation
     *
     * @return the created Entity
     */
    <T> T create( Class<T> entityType, String identity, Object value, Function<T, T> prototypeOpportunity );

    /**
     * Create an Iterable of Entities from an Iterable of Values.
     * <p>
     * If a Value extends {@link Identity} the Entity identity is taken from the Value's state.
     * Else, if a Value's state for {@code Identity} is absent, a new Identity is generated.
     *
     * @param <T>        Value Type
     * @param entityType Entity Type
     * @param values     An Iterable of Values
     *
     * @return the Iterable of created Entities
     */
    <T> Iterable<T> create( Class<T> entityType, Iterable<Object> values );

    /**
     * Create an Iterable of Entities from an Iterable of Values.
     * <p>
     * If a Value extends {@link Identity} the Entity identity is taken from the Value's state.
     * Else, if a Value's state for {@code Identity} is absent, a new Identity is generated.
     *
     * @param <T>                  Value Type
     * @param entityType           Entity Type
     * @param values               An Iterable of Values
     * @param prototypeOpportunity A Function that will be mapped on each Entity prototype before instanciation
     *
     * @return the Iterable of created Entities
     */
    <T> Iterable<T> create( Class<T> entityType, Iterable<Object> values, Function<T, T> prototypeOpportunity );

    /**
     * Update an Entity from a Value.
     *
     * @param entity Entity
     * @param value  Value
     *
     * @throws ClassCastException    If {@code entity} is not an {@link EntityComposite}
     *                               or if {@code value} is not a {@link ValueComposite}
     * @throws NoSuchEntityException If some associated Entity is absent from the EntityStore/UoW
     */
    void update( Object entity, Object value )
        throws ClassCastException, NoSuchEntityException;
}
