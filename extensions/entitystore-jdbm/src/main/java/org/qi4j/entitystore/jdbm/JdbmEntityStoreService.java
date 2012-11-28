/*  Copyright 2008 Rickard Öberg.
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
package org.qi4j.entitystore.jdbm;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.locking.LockingAbstractComposite;
import org.qi4j.library.locking.ReadLockConcern;
import org.qi4j.library.locking.WriteLockConcern;
import org.qi4j.spi.entitystore.*;
import org.qi4j.spi.entitystore.helpers.JSONMapEntityStoreActivation;
import org.qi4j.spi.entitystore.helpers.JSONMapEntityStoreMixin;
import org.qi4j.spi.entitystore.helpers.StateStore;

/**
 * EntityStore service backed by JDBM store.
 * <p>Based on @{@link JSONMapEntityStoreMixin}.</p>
 */
@Concerns( { StateChangeNotificationConcern.class, ConcurrentModificationCheckConcern.class, ReadLockConcern.class, WriteLockConcern.class } )
@Mixins( { JSONMapEntityStoreMixin.class, JdbmEntityStoreMixin.class } )
public interface JdbmEntityStoreService
    extends JdbmEntityStoreActivation,
            JSONMapEntityStoreActivation,
            EntityStore,
            EntityStateVersions,
            StateStore,
            BackupRestore,
            ServiceComposite,
            LockingAbstractComposite,
            Configuration<JdbmConfiguration>
{
}
