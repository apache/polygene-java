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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import org.qi4j.library.framework.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.structure.Module;

/**
 * RMI client implementation of Entity
 */
public class ClientRmiEntityStoreMixin
    implements EntityStore, Activatable
{
    private RemoteEntityStore remote;

    // Activatable implementation
    public void activate() throws Exception
    {
        Registry registry = LocateRegistry.getRegistry( "localhost" );
        remote = (RemoteEntityStore) registry.lookup( ServerRmiEntityStoreService.class.getSimpleName() );
    }

    public void passivate() throws Exception
    {
        remote = null;
    }

    // EntityStore implementation
    public EntityState newEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
    {
        return new DefaultEntityState( identity );
    }

    @WriteLock
    public EntityState getEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
    {
        try
        {
            EntityState state = remote.getEntityState( identity );

            return state;
        }
        catch( IOException e )
        {
            Throwable cause = e.getCause();
            if( cause != null && cause instanceof EntityStoreException )
            {
                throw (EntityStoreException) cause;
            }
            else
            {
                throw new EntityStoreException( e );
            }
        }
    }

    @WriteLock
    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates, Module module ) throws EntityStoreException
    {
        try
        {
            remote.prepare( newStates, loadedStates, removedStates );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }

        return new StateCommitter()
        {
            public void commit()
            {
            }

            public void cancel()
            {
            }
        };
    }

    public Iterator<EntityState> iterator()
    {
        return null;
    }
}