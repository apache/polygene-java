/*  Copyright 2008 Rickard …berg.
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

import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
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
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.EntityId;
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
    public EntityState getEntityState( EntityId identity )
    {
        Class compositeType = module.lookupClass( identity.getCompositeType() );

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
        Map<String, EntityId> associations = new HashMap<String, EntityId>();
        for( String associationName : state.getAssociationNames() )
        {
            EntityId id = state.getAssociation( associationName );
            associations.put( associationName, id );
        }

        // Copy manyassociations
        Map<String, Collection<EntityId>> manyAssociations = new HashMap<String, Collection<EntityId>>();
        for( String associationName : state.getManyAssociationNames() )
        {
            Collection<EntityId> idCollection = state.getManyAssociation( associationName );
            if( idCollection instanceof Set )
            {
                Set<EntityId> collectionCopy = new HashSet<EntityId>( idCollection );
                manyAssociations.put( associationName, collectionCopy );
            }
            else if( idCollection instanceof List )
            {
                List<EntityId> collectionCopy = new ArrayList<EntityId>( idCollection );
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

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<EntityId> removedStates )
    {
        lock.writeLock().lock();
        try
        {
            entityStore.prepare( newStates, loadedStates, removedStates, module ).commit();
            return null;
        }
        catch( EntityStoreException e )
        {
            lock.writeLock().unlock();
            throw e;
        }
    }
}