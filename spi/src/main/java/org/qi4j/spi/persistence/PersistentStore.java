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
package org.qi4j.spi.persistence;

import java.util.List;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.persistence.EntityComposite;

public interface PersistentStore
{
    String getName();

    boolean exists( String identity ) throws PersistenceException;
    
    <T extends EntityComposite> EntityStateHolder<T> newEntityInstance( String identity, CompositeModel<T> compositeModel ) throws PersistenceException;

    <T extends EntityComposite> EntityStateHolder<T> getEntityInstance( String identity, CompositeModel<T> compositeModel ) throws PersistenceException;

    <T extends EntityComposite> List<EntityStateHolder<T>> getEntityInstances( List<String> identities, CompositeModel<T> compositeModel ) throws PersistenceException;

    /** Delete the entity with the given identity from the store.
     *
     * @param identity The identity of the entity to be deleted from the store.
     * @return true if an entity was removed, otherwise false.
     * @throws PersistenceException if there is a physical problem with the connection to the backing store.
     */
    boolean delete( String identity )
        throws PersistenceException;
}
