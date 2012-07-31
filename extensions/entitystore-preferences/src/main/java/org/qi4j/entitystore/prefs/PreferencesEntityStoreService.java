/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.entitystore.prefs;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entitystore.ConcurrentModificationCheckConcern;
import org.qi4j.spi.entitystore.EntityStateVersions;
import org.qi4j.spi.entitystore.EntityStore;

/**
 * EntityStore backed by Preferences API.
 * <p/>
 * A root node is created in the System preferences, whose name
 * is the same as the Application name (default:"Application").
 * <p/>
 * Each entity is stored under its identity name.
 * <p/>
 * Property types are converted to native Preferences API types
 * as much as possible. All others will be serialized to a string using JSON.
 * <p/>
 * ManyAssociations are stored as multi-line strings (one identity
 * per line), and Associations are stored as the identity
 * of the referenced Entity.
 * <p/>
 * The main use of the EntityStore is for storage of ConfigurationComposites for ServiceComposites.
 *
 * @see org.qi4j.api.service.ServiceComposite
 * @see org.qi4j.api.configuration.Configuration
 */
@Concerns( ConcurrentModificationCheckConcern.class )
@Mixins( PreferencesEntityStoreMixin.class )
public interface PreferencesEntityStoreService
    extends EntityStore, ServiceComposite, EntityStateVersions, ServiceActivation
{
}
