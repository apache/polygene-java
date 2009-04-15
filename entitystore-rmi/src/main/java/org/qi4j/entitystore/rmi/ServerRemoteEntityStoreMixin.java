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
package org.qi4j.entitystore.rmi;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.library.locking.WriteLock;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.service.ServiceDescriptor;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * RMI server implementation of EntityStore
 */
public class ServerRemoteEntityStoreMixin
        implements RemoteEntityStore, Activatable
{
    private
    @Uses
    ServiceDescriptor descriptor;
    private
    @This
    RemoteEntityStore remote;
    private
    @This
    ReadWriteLock lock;
    private
    @Structure
    Module module;
    private
    @Structure
    Qi4jSPI spi;
    private
    @Service
    EntityStore entityStore;
    private
    @Service
    ServiceReference<Registry> registry;

    // Activatable implementation
    public void activate() throws Exception
    {
        RemoteEntityStore stub = (RemoteEntityStore) UnicastRemoteObject.exportObject(remote, 0);
        registry.get().bind(descriptor.identity(), stub);
    }

    public void passivate() throws Exception
    {
        if (registry.isActive())
        {
            registry.get().unbind(descriptor.identity());
        }
        UnicastRemoteObject.unexportObject(remote, true);
    }

    // EntityStore implementation
    @WriteLock
    public EntityState getEntityState(EntityReference reference)
            throws IOException
    {
        EntityState state = entityStore.getEntityState(reference);

        Set<EntityTypeReference> entityTypeReferences = copyTypes(state);
        Map<StateName, String> properties = copyProperties(state);
        Map<StateName, EntityReference> associations = copyAssociations(state);
        Map<StateName, ManyAssociationState> manyAssociations = copyManyAssociations(state);

        return new DefaultEntityState(state.version(),
                state.lastModified(),
                reference,
                state.status(),
                new HashSet<EntityTypeReference>(),
                properties,
                associations,
                manyAssociations);
    }

    private Set<EntityTypeReference> copyTypes(EntityState state)
    {
        return null;
    }

    private Map<StateName, ManyAssociationState> copyManyAssociations(EntityState state)
    {
        Map<StateName, ManyAssociationState> manyAssociations = new HashMap<StateName, ManyAssociationState>();
/*
        for (QualifiedName associationName : state.manyAssociationTypes())
        {
            Collection<EntityReference> idCollection = state.getManyAssociation(associationName);
            if (idCollection instanceof Set)
            {
                Set<EntityReference> collectionCopy = new HashSet<EntityReference>(idCollection);
                manyAssociations.put(associationName, collectionCopy);
            } else if (idCollection instanceof List)
            {
                List<EntityReference> collectionCopy = new ArrayList<EntityReference>(idCollection);
                manyAssociations.put(associationName, collectionCopy);
            }
        }
*/
        return manyAssociations;
    }

    private Map<StateName, EntityReference> copyAssociations(EntityState state)
    {
        Map<StateName, EntityReference> associations = new HashMap<StateName, EntityReference>();
/*
        for (QualifiedName associationName : state.associationTypes())
        {
            EntityReference id = state.getAssociation(associationName);
            manyAssociations.put(associationName, id);
        }
*/
        return associations;
    }

    private Map<StateName, String> copyProperties(EntityState state)
    {
        Map<StateName, String> properties = new HashMap<StateName, String>();
/*
        for (QualifiedName propertyName : state.propertyTypes())
        {
            Object value = state.getProperty(propertyName, propertyType());
            properties.put(propertyName, value);
        }
*/
        return properties;
    }

    public void prepare(Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<EntityReference> removedStates)
    {
        lock.writeLock().lock();
        try
        {
            final StateCommitter committer = entityStore.prepare(newStates, loadedStates, removedStates);
            try
            {
                committer.commit();
            }
            catch (EntityStoreException e)
            {
                committer.cancel();
                throw e;
            }

        }

        finally
        {
            lock.writeLock().unlock();
        }
    }
}