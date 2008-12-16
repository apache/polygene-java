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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.locking.WriteLock;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.qi4j.api.structure.Module;

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
    private @Service ServiceReference<Registry> registry;

    // Activatable implementation
    public void activate() throws Exception
    {
        RemoteEntityStore stub = (RemoteEntityStore) UnicastRemoteObject.exportObject( remote, 0 );
        registry.get().bind( descriptor.identity(), stub );
    }

    public void passivate() throws Exception
    {
        if( registry.isActive() )
        {
            registry.get().unbind( descriptor.identity() );
        }
        UnicastRemoteObject.unexportObject( remote, true );
    }

    // EntityStore implementation
    @WriteLock
    public EntityState getEntityState( QualifiedIdentity identity )
        throws IOException
    {
        EntityState state = getEntityState( entityStore, identity );

        Map<String, Object> properties = copyProperties( state );
        Map<String, QualifiedIdentity> associations = copyAssociations( state );
        Map<String, Collection<QualifiedIdentity>> manyAssociations = copyManyAssociations( state );

        return new DefaultEntityState( state.version(),
                                       state.lastModified(),
                                       identity,
                                       state.status(),
                                       getEntityType( identity ),
                                       properties,
                                       associations,
                                       manyAssociations );
    }

    private Map<String, Collection<QualifiedIdentity>> copyManyAssociations( EntityState state )
    {
        Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
        for( String associationName : state.manyAssociationNames() )
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
        for( String associationName : state.associationNames() )
        {
            QualifiedIdentity id = state.getAssociation( associationName );
            associations.put( associationName, id );
        }
        return associations;
    }

    private Map<String, Object> copyProperties( EntityState state )
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        for( String propertyName : state.propertyNames() )
        {
            Object value = state.getProperty( propertyName );
            properties.put( propertyName, value );
        }
        return properties;
    }

    private EntityType getEntityType( QualifiedIdentity identity )
    {
        final String typeName = identity.type();
        try
        {
            return spi.getEntityDescriptor( module.classLoader().loadClass( typeName ), module ).entityType();
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( "Error accessing CompositeType for type " + typeName, e );
        }
    }

    public void prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates )
    {
        lock.writeLock().lock();
        try
        {
            final StateCommitter committer = entityStore.prepare( newStates, loadedStates, removedStates );
            try
            {
                committer.commit();
            }
            catch( EntityStoreException e )
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

    public EntityState getEntityState( EntityStore entityStore, QualifiedIdentity qid )
        throws EntityStoreException
    {
        EntityState entityState = null;
        do
        {
            try
            {
                entityState = entityStore.getEntityState( qid );
            }
            catch( UnknownEntityTypeException e )
            {
                // Check if it is this type that the store doesn't understand
                EntityType entityType = getEntityType( qid );
                if( e.getMessage().equals( entityType.type() ) )
                {
                    entityStore.registerEntityType( entityType );
                    // Try again
                }
                else
                {
                    throw e; // ???
                }
            }
        }
        while( entityState == null );

        return entityState;
    }
}