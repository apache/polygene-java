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

package org.apache.zest.spi.entitystore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;

/**
 * Entity versions state.
 */
@Mixins( EntityStateVersions.EntityStateVersionsMixin.class )
public interface EntityStateVersions
{
    void forgetVersions( Iterable<EntityState> states );

    void rememberVersion( EntityReference identity, String version );

    void checkForConcurrentModification( Iterable<EntityState> loaded, Instant currentTime )
        throws ConcurrentEntityStateModificationException;

    /**
     * Entity versions state mixin.
     */
    class EntityStateVersionsMixin
        implements EntityStateVersions
    {
        @This
        private EntityStore store;

        private final Map<EntityReference, String> versions = new WeakHashMap<>();

        @Override
        public synchronized void forgetVersions( Iterable<EntityState> states )
        {
            for( EntityState state : states )
            {
                versions.remove( state.identity() );
            }
        }

        @Override
        public synchronized void rememberVersion( EntityReference identity, String version )
        {
            versions.put( identity, version );
        }

        @Override
        public synchronized void checkForConcurrentModification( Iterable<EntityState> loaded,
                                                                 Instant currentTime
        )
            throws ConcurrentEntityStateModificationException
        {
            List<EntityReference> changed = null;
            for( EntityState entityState : loaded )
            {
                if( entityState.status().equals( EntityStatus.NEW ) )
                {
                    continue;
                }

                String storeVersion = versions.get( entityState.identity() );
                if( storeVersion == null )
                {
                    EntityStoreUnitOfWork unitOfWork = store.newUnitOfWork( entityState.entityDescriptor().module(), Usecase.DEFAULT, currentTime );
                    storeVersion = unitOfWork.versionOf( entityState.identity() );
                    unitOfWork.discard();
                }

                if( !entityState.version().equals( storeVersion ) )
                {
                    if( changed == null )
                    {
                        changed = new ArrayList<>();
                    }
                    changed.add( entityState.identity() );
                }
            }

            if( changed != null )
            {
                throw new ConcurrentEntityStateModificationException( changed );
            }
        }
    }
}
