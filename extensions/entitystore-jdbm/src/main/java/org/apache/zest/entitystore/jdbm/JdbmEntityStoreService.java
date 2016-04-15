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
package org.apache.zest.entitystore.jdbm;

import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.library.locking.LockingAbstractComposite;
import org.apache.zest.library.locking.ReadLockConcern;
import org.apache.zest.library.locking.WriteLockConcern;
import org.apache.zest.spi.entitystore.*;
import org.apache.zest.spi.entitystore.helpers.JSONMapEntityStoreActivation;
import org.apache.zest.spi.entitystore.helpers.JSONMapEntityStoreMixin;
import org.apache.zest.spi.entitystore.helpers.StateStore;

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
