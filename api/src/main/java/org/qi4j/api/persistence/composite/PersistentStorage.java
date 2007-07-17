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
package org.qi4j.api.persistence.composite;

import java.io.Serializable;
import org.qi4j.api.persistence.composite.EntityComposite;
import org.qi4j.api.persistence.PersistenceException;

/**
 * Persistent repositories must implement this.
 */
public interface PersistentStorage
{
    // TODO: Add MixinModel to allow for call from CompositeInvocationHandler
    void update( EntityComposite aProxy, Serializable aMixin )
        throws PersistenceException;

    // TODO: Add MixinModel to allow for call from CompositeInvocationHandler
    void read( EntityComposite aProxy )
        throws PersistenceException;

    <T extends EntityComposite> EntityComposite getEntity( String anIdentity, Class<T> aType );

    <T extends EntityComposite> void putEntity( EntityComposite composite );

    void delete( EntityComposite aProxy )
        throws PersistenceException;
}
