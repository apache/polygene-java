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
package org.qi4j.entity.rmi;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.This;
import org.qi4j.composite.scope.Uses;
import org.qi4j.library.framework.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateInstance;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.util.ListMap;
import org.qi4j.structure.Module;
import org.qi4j.structure.ServiceMap;

/**
 * RMI server implementation of EntityStore
 */
public class ServerRemoteEntityStoreMixin
    implements RemoteEntityStore, Activatable
{
    private @Uses ServiceDescriptor descriptor;
    private @This RemoteEntityStore remote;
    private @This ReadWriteLock lock;
    private @Structure Module module;
    private @Service EntityStore entityStore;
    private @Service Registry registry;

    // Activatable implementation
    public void activate() throws Exception
    {
        RemoteEntityStore stub = (RemoteEntityStore) UnicastRemoteObject.exportObject( remote, 0 );
        registry.rebind( descriptor.identity(), stub );
    }

    public void passivate() throws Exception
    {
        registry.unbind( descriptor.identity() );
        UnicastRemoteObject.unexportObject( remote, true );
    }

    // EntityStore implementation
    @WriteLock
    public EntityState getEntityState( QualifiedIdentity identity )
        throws IOException
    {
        try
        {
            Class compositeType = module.findClass( identity.getCompositeType() );
        }
        catch( ClassNotFoundException e )
        {
            throw (IOException) new IOException().initCause( e );
        }

        // TODO How should we get ahold of the CompositeDescriptor?
        EntityState state = entityStore.getEntityState( null, identity );

        // Copy properties
        Map<String, Object> properties = new HashMap<String, Object>();
        for( String propertyName : state.getPropertyNames() )
        {
            Object value = state.getProperty( propertyName );
            properties.put( propertyName, value );
        }

        // Copy associations
        Map<String, QualifiedIdentity> associations = new HashMap<String, QualifiedIdentity>();
        for( String associationName : state.getAssociationNames() )
        {
            QualifiedIdentity id = state.getAssociation( associationName );
            associations.put( associationName, id );
        }

        // Copy manyassociations
        Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
        for( String associationName : state.getManyAssociationNames() )
        {
            Collection<QualifiedIdentity> idCollection = state.getManyAssociation( associationName );
            if( idCollection instanceof Set )
            {
                Set<QualifiedIdentity> collectionCopy = new HashSet<QualifiedIdentity>( idCollection );
                manyAssociations.put( associationName, collectionCopy );
            }
            else if( idCollection instanceof List )
            {
                List<QualifiedIdentity> collectionCopy = new ArrayList<QualifiedIdentity>( idCollection );
                manyAssociations.put( associationName, collectionCopy );
            }
        }

        EntityStateInstance entityState = new EntityStateInstance( state.getEntityVersion(),
                                                                   identity,
                                                                   state.getStatus(),
                                                                   properties,
                                                                   associations,
                                                                   manyAssociations );
        return entityState;
    }

    public void prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates )
    {
        lock.writeLock().lock();
        try
        {
            ServiceMap<EntityStore> storeMap = new ServiceMap<EntityStore>( module, EntityStore.class );
            Set<EntityStore> stores = new HashSet<EntityStore>();

            ListMap<EntityStore, EntityState> newStoreState = new ListMap<EntityStore, EntityState>();
            for( EntityState newState : newStates )
            {
                QualifiedIdentity id = newState.getIdentity();
                Class compositeType = module.findClass( id.getCompositeType() );
                EntityStore compositeStore = storeMap.getService( compositeType );
                newStoreState.add( compositeStore, newState );
                stores.add( compositeStore );
            }

            ListMap<EntityStore, EntityState> loadedStoreState = new ListMap<EntityStore, EntityState>();
            for( EntityState loadedState : loadedStates )
            {
                QualifiedIdentity id = loadedState.getIdentity();
                Class compositeType = module.findClass( id.getCompositeType() );
                EntityStore compositeStore = storeMap.getService( compositeType );
                loadedStoreState.add( compositeStore, loadedState );
                stores.add( compositeStore );
            }

            ListMap<EntityStore, QualifiedIdentity> removedStoreState = new ListMap<EntityStore, QualifiedIdentity>();
            for( QualifiedIdentity removedState : removedStates )
            {
                Class compositeType = module.findClass( removedState.getCompositeType() );
                EntityStore compositeStore = storeMap.getService( compositeType );
                removedStoreState.add( compositeStore, removedState );
                stores.add( compositeStore );
            }

            // Call all stores with their respective subsets
            List<StateCommitter> committers = new ArrayList<StateCommitter>();
            try
            {
                for( EntityStore store : stores )
                {
                    Iterable<EntityState> newState = newStoreState.get( store );
                    Iterable<EntityState> loadedState = loadedStoreState.get( store );
                    Iterable<QualifiedIdentity> removedState = removedStoreState.get( store );

                    committers.add( store.prepare( newState == null ? Collections.EMPTY_LIST : newState,
                                                   loadedState == null ? Collections.EMPTY_LIST : loadedState,
                                                   removedState == null ? Collections.EMPTY_LIST : removedState,
                                                   module ) );
                }

                for( StateCommitter committer : committers )
                {
                    committer.commit();
                }
            }
            catch( EntityStoreException e )
            {
                for( StateCommitter committer : committers )
                {
                    committer.cancel();
                }

                throw e;
            }
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( e );
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
}