/*  Copyright 2010 Niclas Hedhman
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
package org.qi4j.entitystore.gae2;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.locking.LockingAbstractComposite;
import org.qi4j.spi.entitystore.ConcurrentModificationCheckConcern;
import org.qi4j.spi.entitystore.EntityStateVersions;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.StateChangeNotificationConcern;
import org.qi4j.spi.entitystore.helpers.JSONMapEntityStoreActivation;
import org.qi4j.spi.entitystore.helpers.JSONMapEntityStoreMixin;
import org.qi4j.spi.entitystore.helpers.StateStore;

/**
 * EntityStore service backed by Google AppEngine's low-level store.
 * <p>Based on @{@link JSONMapEntityStoreMixin}.</p>
 */
@Concerns( { StateChangeNotificationConcern.class, ConcurrentModificationCheckConcern.class } )
@Mixins( { JSONMapEntityStoreMixin.class, GaeEntityStoreMixin.class } )
public interface GaeEntityStoreService
    extends GaeEntityStoreActivation,
            JSONMapEntityStoreActivation,
            EntityStore,
            EntityStateVersions,
            StateStore,
            ServiceComposite,
            LockingAbstractComposite,
            Configuration
{
}