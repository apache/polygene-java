/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.extension.persistence.quick;

import org.qi4j.api.CompositeFactory;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.persistence.composite.EntityComposite;

public final class PersitentCloneableModifier<T extends EntityComposite>
    implements org.qi4j.api.persistence.Cloneable<T>
{
    @Modifies org.qi4j.api.persistence.Cloneable<T> cloneable;
    @Uses EntityComposite entity;
    @Dependency CompositeFactory factory;

    public T clone()
    {
        T cloned = cloneable.clone();
        cloned.setIdentity( entity.getIdentity() + "cloned" );
        entity.getEntityRepository().create( factory.dereference( cloned ) );
        return cloned;
    }
}
