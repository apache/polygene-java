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

package org.qi4j.spi.entitystore;

import org.qi4j.api.Qi4j;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Concern that helps EntityStores do concurrent modification checks.
 * <p/>
 * It caches the versions of state that it loads, and forgets them when
 * the state is committed. For normal operation this means that it does
 * not have to go down to the underlying store to get the current version.
 * Whenever there is a concurrent modification the store will most likely
 * have to check with the underlying store what the current version is.
 */
public abstract class ConcurrentModificationCheckConcern
    extends ConcernOf<EntityStore>
    implements EntityStore
{
    @This
    private EntityStateVersions versions;
    @Structure
    private Qi4j api;

    @Override
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module, long currentTime )
    {
        final EntityStoreUnitOfWork uow = next.newUnitOfWork( usecase, module, currentTime );
        return new ConcurrentCheckingEntityStoreUnitOfWork( uow, api.dereference( versions ), module, currentTime );
    }

    private static class ConcurrentCheckingEntityStoreUnitOfWork
        implements EntityStoreUnitOfWork
    {
        private final EntityStoreUnitOfWork uow;
        private EntityStateVersions versions;
        private Module module;
        private long currentTime;

        private List<EntityState> loaded = new ArrayList<EntityState>();

        private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(  );

        public ConcurrentCheckingEntityStoreUnitOfWork( EntityStoreUnitOfWork uow,
                                                        EntityStateVersions versions,
                                                        Module module,
                                                        long currentTime
        )
        {
            this.uow = uow;
            this.versions = versions;
            this.module = module;
            this.currentTime = currentTime;
        }

        @Override
        public String identity()
        {
            return uow.identity();
        }

        @Override
        public long currentTime()
        {
            return uow.currentTime();
        }

        @Override
        public EntityState newEntityState( EntityReference anIdentity, EntityDescriptor entityDescriptor )
            throws EntityStoreException
        {
            return uow.newEntityState( anIdentity, entityDescriptor );
        }

        @Override
        public StateCommitter applyChanges()
            throws EntityStoreException
        {
            lock.writeLock().lock();

            try
            {
               versions.checkForConcurrentModification( loaded, module, currentTime );

               final StateCommitter committer = uow.applyChanges();

               return new StateCommitter()
               {
                   @Override
                   public void commit()
                   {
                       committer.commit();
                       versions.forgetVersions( loaded );

                       lock.writeLock().unlock();
                   }

                   @Override
                   public void cancel()
                   {
                       committer.cancel();
                       versions.forgetVersions( loaded );

                       lock.writeLock().unlock();
                   }
               };
            } catch( EntityStoreException e )
            {
               lock.writeLock().unlock();
               throw e;
            }
        }

        @Override
        public void discard()
        {
            try
            {
                uow.discard();
            }
            finally
            {
                lock.writeLock().lock();

                try
                {
                   versions.forgetVersions( loaded );
                } finally
                {
                   lock.writeLock().unlock();
                }
            }
        }

        @Override
        public EntityState entityStateOf( EntityReference anIdentity )
            throws EntityStoreException, EntityNotFoundException
        {
            lock.readLock().lock();

            try
            {
               EntityState entityState = uow.entityStateOf( anIdentity );
               versions.rememberVersion( entityState.identity(), entityState.version() );
               loaded.add( entityState );
               return entityState;
            } finally
            {
               lock.readLock().unlock();
            }
        }
    }
}
