/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.bootstrap.migration;

import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;

/**
 * Interface for migration rule implementations. When an Entity is found to have an outdated Entity version
 * the migration rules for converting the data from the current version to the desired version will be executed
 * on the EntityState of the Entity.
 */
interface MigrationRule
{
    void migrate(EntityState state, EntityType from, EntityType to);
}
