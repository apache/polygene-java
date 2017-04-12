/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.api.association;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;

/**
 * Association to a collection of entities.
 */
public interface ManyAssociation<T> extends Iterable<T>, AbstractAssociation
{
    /**
     * Check is the entity is part of this {@code ManyAssociation}.
     *
     * @param entity The entity to be checking for.
     * @return true if there is an entity in this ManyAssociation with the same {@link Identity} as the given
     * entity , otherwise false.
     */
    boolean contains( T entity );

    /** Adds an entity reference representing the given entity to the {@code index} slot of this collection.
     * <p>
     *     {@code index=0} represents the beginning of the collection and if the {@code index} is equal or larger
     *     than the length of the collection, the entity reference will be added to the end.
     * </p>
     * @param entity The entity whose entity reference is to be added to this collection.
     * @param index the position for the entity to be inserted at.
     * @return true if the entity reference has been added, false otherwise.
     */
    boolean add( int index, T entity );

    /** Adds an entity reference representing the given entity to the end of this collection.
     *
     * @param entity The entity whose entity reference is to be added to this collection.
     * @return true if the entity reference has been added, false otherwise.
     */
    boolean add( T entity );

    /**
     * Removes the given entity from this {@code ManyAssociation}.
     * <p>
     *     The entity reference representing the given entity is removed from this collection.
     * </p>
     * @param entity The entity reference to be removed.
     * @return true if an entity reference was removed, otherwise false
     */
    boolean remove( T entity );

    /** Fetch the entity refrence at the given index and fetch the entity from the entity store.
     *
     * @param index The index location in the collection of the entity reference to be fetched.
     * @return The retrieved entity that the entity reference of this collection represents.
     */
    T get( int index );

    /**
     * Returns the number of references in this association.
     * @return the number of references in this association.
     */
    int count();

    /**
     * Fetches all entities represented by entity references in this collection and returns a List of such
     * entities.
     * <p>
     *     Multiple references to the same entity will be present multiple times in the List, unlike {@link #toSet()}.
     *     The order in which the entities were added to this collection is preserved.
     * </p>
     * @return a List of entities represented by the entity references in this collection.
     */
    List<T> toList();

    /**
     * Fetches all entities represented by entity references in this collection and returns a Set of such
     * entities.
     * <p>
     *     Multiple references to the same entity will NOT be present, unlike {@link #toList()}. Sets are defined
     *     to only contain any particular object once. Order is not preserved.
     * </p>
     * @return a Set of entities represented by the entity references in this collection.
     */
    Set<T> toSet();

    /**
     * Returns a stream of the references to the associated entities.
     * @return the references to the associated entities.
     */
    Stream<EntityReference> references();
}
