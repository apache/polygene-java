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

package org.qi4j.spi.entitystore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;

/**
 * Entity versions state.
 */
@Mixins( EntityStateVersions.EntityStateVersionsMixin.class )
public interface EntityStateVersions
{
    void forgetVersions( Iterable<EntityState> states );

    void rememberVersion( EntityReference identity, String version );

    void checkForConcurrentModification( Iterable<EntityState> loaded, Module module, long currentTime )
        throws ConcurrentEntityStateModificationException;

    /**
     * Entity versions state mixin.
     */
    class EntityStateVersionsMixin
        implements EntityStateVersions
    {
        @This
        private EntityStore store;

        private final Map<EntityReference, String> versions = new WeakHashMap<EntityReference, String>();

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
                                                                 Module module,
                                                                 long currentTime
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
                    EntityStoreUnitOfWork unitOfWork = store.newUnitOfWork( Usecase.DEFAULT, module, currentTime );
                    EntityState state = unitOfWork.entityStateOf( entityState.identity() );
                    storeVersion = state.version();
                    unitOfWork.discard();
                }

                if( !entityState.version().equals( storeVersion ) )
                {
                    if( changed == null )
                    {
                        changed = new ArrayList<EntityReference>();
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
