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
package org.qi4j.runtime.entity;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.UnitOfWork;

public final class EntityMixin
    implements Entity
{
    @This private EntityComposite meAsEntity;

    public boolean isReference()
    {
        EntityInstance instance = EntityInstance.getEntityInstance( meAsEntity );
        return instance.isReference();
    }

    public UnitOfWork unitOfWork()
    {
        EntityInstance instance = EntityInstance.getEntityInstance( meAsEntity );
        return instance.unitOfWork();
    }
}
