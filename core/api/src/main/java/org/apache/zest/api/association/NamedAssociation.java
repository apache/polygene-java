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
package org.apache.zest.api.association;

import java.util.Map;
import java.util.stream.Stream;
import org.apache.zest.api.entity.EntityReference;

/**
 * Association to named Entities.
 * The Iterable&lt;String&gt; returns the names in the association set.
 * @param <T> Parameterized associatee type
 */
public interface NamedAssociation<T>
    extends Iterable<String>, AbstractAssociation
{
    /**
     * @return The number of named associations in this NamedAssociation.
     */
    int count();

    /**
     * Checks if there is an association with the given name.
     * @param name The name of the association we are checking if it exists.
     * @return true if it exists, false otherwise
     */
    boolean containsName( String name );

    /**
     * Adds a named assocation.
     * @param name The name of the association.
     * @param entity The entity for this named association.
     * @return true if putted, false otherwise
     */
    boolean put( String name, T entity );

    /**
     * Remove a named association.
     * @param name The name of the association.
     * @return true if removed, false otherwise
     */
    boolean remove( String name );

    /**
     * Retrieves a named association.
     * @param name The name of the association.
     * @return The entity that has previously been associated.
     */
    T get( String name );

    /**
     * Checks if the entity is present.
     * Note that this is potentially a very slow operation, depending on the size of the NamedAssociation.
     * @param entity The entity to look for.
     * @return The name of the entity if found, otherwise null.
     */
    String nameOf( T entity );

    /**
     * @return A fully populated Map with the content of this NamedAssociation.
     */
    Map<String, T> toMap();

    /**
     * Returns a stream of the references to the associated entities.
     * @return the references to the associated entities.
     */
    Stream<Map.Entry<String, EntityReference>> references();

    /** Returns the EntityReference for the Association with the given name.
     *
     * @param name The name of the association to return the EntityReference for
     * @return The EntityReference of the association.
     */
    EntityReference referenceOf( String name );
}
