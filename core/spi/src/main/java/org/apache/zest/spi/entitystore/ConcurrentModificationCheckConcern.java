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
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.spi.entity.EntityState;

/**
 * Concern that helps EntityStores do concurrent modification checks.
 * <p>
 * It caches the versions of state that it loads, and forgets them when
 * the state is committed. For normal operation this means that it does
 * not have to go down to the underlying store to get the current version.
 * Whenever there is a concurrent modification the store will most likely
 * have to check with the underlying store what the current version is.
 * </p>
 */
public abstract class ConcurrentModificationCheckConcern
    extends ConcernOf<EntityStore>
    implements EntityStore
{
    @This
    private EntityStateVersions versions;

    @Structure
    private ZestAPI api;

    @Override
    public EntityStoreUnitOfWork newUnitOfWork( ModuleDescriptor module, Usecase usecase, Instant currentTime )
    {
        final EntityStoreUnitOfWork uow = next.newUnitOfWork( module, usecase, currentTime );
        return new ConcurrentCheckingEntityStoreUnitOfWork( uow, api.dereference( versions ), currentTime );
    }

    private static class ConcurrentCheckingEntityStoreUnitOfWork
        implements EntityStoreUnitOfWork
    {
        private final EntityStoreUnitOfWork uow;
        private EntityStateVersions versions;
        private Instant currentTime;

        private HashSet<EntityState> loaded = new HashSet<>();

        private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public ConcurrentCheckingEntityStoreUnitOfWork( EntityStoreUnitOfWork uow,
                                                        EntityStateVersions versions,
                                                        Instant currentTime
        )
        {
            this.uow = uow;
            this.versions = versions;
            this.currentTime = currentTime;
        }

        @Override
        public String identity()
        {
            return uow.identity();
        }

        @Override
        public Instant currentTime()
        {
            return uow.currentTime();
        }

        @Override
        public EntityState newEntityState( EntityReference anIdentity,
                                           EntityDescriptor entityDescriptor
        )
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
                versions.checkForConcurrentModification( loaded, currentTime );

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
            }
            catch( EntityStoreException e )
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
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }
        }

        @Override
        public Usecase usecase()
        {
            return uow.usecase();
        }

        @Override
        public ModuleDescriptor module()
        {
            return uow.module();
        }

        @SuppressWarnings( "DuplicateThrows" )
        @Override
        public EntityState entityStateOf( ModuleDescriptor module, EntityReference anIdentity )
            throws EntityStoreException, EntityNotFoundException
        {
            lock.readLock().lock();

            try
            {
                EntityState entityState = uow.entityStateOf( module, anIdentity );
                versions.rememberVersion( entityState.identity(), entityState.version() );
                loaded.add( entityState );
                return entityState;
            }
            finally
            {
                lock.readLock().unlock();
            }
        }


        @Override
        public String versionOf( EntityReference anIdentity )
            throws EntityStoreException
        {
            return uow.versionOf( anIdentity );
        }
    }
}
