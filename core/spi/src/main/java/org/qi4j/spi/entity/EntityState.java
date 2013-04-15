/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.entity;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;

/**
 * State holder for Entities.
 */
public interface EntityState
{
    /**
     * Returns the identity of the entity that this EntityState represents.
     *
     * @return the identity of the entity that this EntityState represents.
     */
    EntityReference identity();

    /**
     * Version of the entity. This is managed by the EntityStore.
     * <p/>
     * If the underlying EntityStore does not support versioning, then version
     * must always be set to 0.
     *
     * @return version of the entity
     */
    String version();

    /**
     * Last modified timestamp of the entity. This is managed by the EntityStore.
     * <p/>
     * If the underlying EntityStore does not support timestamping, then last modified
     * must always be set to the current time.
     *
     * @return last modified timestamp of the entity, as defined by System.currentTimeMillis()
     */
    long lastModified();

    /**
     * Remove the entity represented by this EntityState when the unit of work is completed.
     */
    void remove();

    /**
     * The status of this EntityState
     *
     * @return the status
     */
    EntityStatus status();

    boolean isAssignableTo( Class<?> type );

    EntityDescriptor entityDescriptor();

    Object propertyValueOf( QualifiedName stateName );

    void setPropertyValue( QualifiedName stateName, Object json );

    EntityReference associationValueOf( QualifiedName stateName );

    void setAssociationValue( QualifiedName stateName, EntityReference newEntity );

    ManyAssociationState manyAssociationValueOf( QualifiedName stateName );
}
