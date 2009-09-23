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
package org.qi4j.spi.entitystore;

import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;

/**
 * Interface that must be implemented by store for persistent state of EntityComposites.
 */
public interface EntityStore
{
    EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module );

    EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor, Module module );

    interface EntityStateVisitor
    {
        void visitEntityState( EntityState entityState );
    }
}
