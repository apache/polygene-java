/* Copyright 2008 Neo Technology, http://neotechnology.com.
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
package org.qi4j.entitystore.neo4j.state;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.entitystore.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.StateCommitter;

import java.util.Iterator;

public interface NeoEntityStateFactory
{
    CommittableEntityState createEntityState(NeoIdentityIndex idIndex, LoadedDescriptor descriptor, EntityReference reference, EntityStatus status);

    StateCommitter prepareCommit(NeoIdentityIndex idIndex, Iterable<CommittableEntityState> updated, Iterable<EntityReference> removed) throws EntityStoreException;

    Iterator<CommittableEntityState> iterator(NeoIdentityIndex idIndex);
}
