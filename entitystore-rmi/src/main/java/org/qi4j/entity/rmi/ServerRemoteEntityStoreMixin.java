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
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.injection.scope.Uses;
import org.qi4j.library.framework.locking.WriteLock;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.util.ServiceMap;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.util.ListMap;
import org.qi4j.structure.Module;

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
    private @Structure Qi4jSPI spi;
    private @Service EntityStore entityStore;
    private @Service Registry registry;

    // Activatable implementation
    public void activate() throws Exception
    {
        RemoteEntityStore stub = (RemoteEntityStore) UnicastRemoteObject.exportObject( remote, 0 );
        registry.bind( descriptor.identity(), stub );
    }

    public void passivate() throws Exception
    {
        // registry.unbind( descriptor.identity() );
        UnicastRemoteObject.unexportObject( remote, true );
    }

    // EntityStore implementation
    @WriteLock
    public EntityState getEntityState( QualifiedIdentity identity )
        throws IOException
    {
        final Class<?> compositeType = getCompositeTypeClass( identity );

        final CompositeDescriptor compositeDescriptor = spi.getCompositeDescriptor( compositeType, module );
        EntityState state = entityStore.getEntityState( compositeDescriptor, identity );

        Map<String, Object> properties = copyProperties( state );
        Map<String, QualifiedIdentity> associations = copyAssociations( state );
        Map<String, Collection<QualifiedIdentity>> manyAssociations = copyManyAssociations( state );

        return new DefaultEntityState( state.getEntityVersion(),
                                       identity,
                                       state.getStatus(),
                                       properties,
                                       associations,
                                       manyAssociations );
    }

    private Map<String, Collection<QualifiedIdentity>> copyManyAssociations( EntityState state )
    {
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
        return manyAssociations;
    }

    private Map<String, QualifiedIdentity> copyAssociations( EntityState state )
    {
        Map<String, QualifiedIdentity> associations = new HashMap<String, QualifiedIdentity>();
        for( String associationName : state.getAssociationNames() )
        {
            QualifiedIdentity id = state.getAssociation( associationName );
            associations.put( associationName, id );
        }
        return associations;
    }

    private Map<String, Object> copyProperties( EntityState state )
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        for( String propertyName : state.getPropertyNames() )
        {
            Object value = state.getProperty( propertyName );
            properties.put( propertyName, value );
        }
        return properties;
    }

    private Class<?> getCompositeTypeClass( QualifiedIdentity identity )
    {
        final String typeName = identity.type();
        final Class type = ( (ModuleInstance) module ).findClassForName( typeName );
        if( type == null )
        {
            throw new EntityStoreException( "Error accessing CompositeType for type " + typeName );
        }
        return type;
    }

    public void prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates )
    {
        lock.writeLock().lock();
        try
        {
            ServiceMap<EntityStore> storeMap = new ServiceMap<EntityStore>( (ModuleInstance) module, EntityStore.class );

            ListMap<EntityStore, EntityState> newStoreState = new ListMap<EntityStore, EntityState>();
            for( EntityState newState : newStates )
            {
                EntityStore store = getStoreForState( storeMap, newState );
                newStoreState.add( store, newState );
            }

            ListMap<EntityStore, EntityState> loadedStoreState = new ListMap<EntityStore, EntityState>();
            for( EntityState loadedState : loadedStates )
            {
                EntityStore store = getStoreForState( storeMap, loadedState );
                loadedStoreState.add( store, loadedState );
            }

            ListMap<EntityStore, QualifiedIdentity> removedStoreState = new ListMap<EntityStore, QualifiedIdentity>();
            for( QualifiedIdentity removedIdentity : removedStates )
            {
                final EntityStore store = getStoreForIdentity( storeMap, removedIdentity );
                removedStoreState.add( store, removedIdentity );
            }

            Set<EntityStore> stores = new HashSet<EntityStore>( newStoreState.keySet() );
            stores.addAll( loadedStoreState.keySet() );
            stores.addAll( removedStoreState.keySet() );

            // Call all stores with their respective subsets
            List<StateCommitter> committers = new ArrayList<StateCommitter>();
            try
            {
                for( EntityStore store : stores )
                {
                    Iterable<EntityState> newState = newStoreState.get( store );
                    Iterable<EntityState> loadedState = loadedStoreState.get( store );
                    Iterable<QualifiedIdentity> removedState = removedStoreState.get( store );

                    committers.add( store.prepare( newState == null ? Collections.<EntityState>emptyList() : newState,
                                                   loadedState == null ? Collections.<EntityState>emptyList() : loadedState,
                                                   removedState == null ? Collections.<QualifiedIdentity>emptyList() : removedState,
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
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private EntityStore getStoreForState( ServiceMap<EntityStore> storeMap, EntityState state )
    {
        QualifiedIdentity id = state.getIdentity();
        return getStoreForIdentity( storeMap, id );
    }

    private EntityStore getStoreForIdentity( ServiceMap<EntityStore> storeMap, QualifiedIdentity id )
    {
        Class compositeType = getCompositeTypeClass( id );
        return storeMap.getService( compositeType );
    }
}