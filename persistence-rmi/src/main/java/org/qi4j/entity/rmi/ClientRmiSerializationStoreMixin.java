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

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.library.framework.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.serialization.SerializationStore;

/**
 * RMI client implementation of SerializationStore
 */
public class ClientRmiSerializationStoreMixin
    implements SerializationStore, Activatable
{
    private @ThisCompositeAs ReadWriteLock lock;

    private SerializationStore remote;

    // Activatable implementation
    public void activate() throws Exception
    {
        if( remote == null )
        {
            Registry registry = LocateRegistry.getRegistry( "localhost" );
            remote = (SerializationStore) registry.lookup( "entityStore" );
        }
    }

    public void passivate() throws Exception
    {
        remote = null;
    }

    // SerializationStore implementation
    @WriteLock
    public SerializableState get( EntityId entityIdId, CompositeBuilderFactory compositeBuilderFactory ) throws IOException
    {
        SerializableState serializableState = remote.get( entityIdId, null );

        return serializableState;
    }

    @WriteLock
    public boolean contains( EntityId entityIdId ) throws IOException
    {
        return remote.contains( entityIdId );
    }

    public StateCommitter prepare( Map<EntityId, SerializableState> newEntities, Map<EntityId, SerializableState> updatedEntities, Iterable<EntityId> removedEntities )
        throws IOException
    {
        lock.writeLock().lock();

        try
        {
            return remote.prepare( newEntities, updatedEntities, removedEntities );
        }
        catch( IOException e )
        {
            lock.writeLock().unlock();
            throw e;
        }
    }
}