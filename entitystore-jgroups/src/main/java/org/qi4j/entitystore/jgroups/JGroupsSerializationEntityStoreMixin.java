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

package org.qi4j.entitystore.jgroups;

import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.Streams;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.helpers.MapEntityStore;
import org.qi4j.spi.service.ServiceDescriptor;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * JGroups implementation of EntityStore
 */
public class JGroupsSerializationEntityStoreMixin
    implements MapEntityStore, Activatable
{
    private @This ReadWriteLock lock;

    private ReplicatedHashMap<String, byte[]> replicatedMap;
    private JChannel channel;
    private @Uses ServiceDescriptor descriptor;

    // Activatable implementation
    public void activate() throws Exception
    {
        channel = new JChannel();
        channel.connect( "entitystore" );
        replicatedMap = new ReplicatedHashMap<String, byte[]>( channel, false );
        replicatedMap.setBlockingUpdates( true );
    }

    public void passivate() throws Exception
    {
        channel.close();
    }

    public boolean contains(EntityReference entityReference) throws EntityStoreException
    {
        return replicatedMap.containsKey(entityReference.toString());
    }

    public void update(Map<EntityReference, InputStream> newEntities, Map<EntityReference, InputStream> updatedEntities, Iterable<EntityReference> removedEntities)
    {
        for (Map.Entry<EntityReference, InputStream> entityReferenceInputStreamEntry : newEntities.entrySet())
        {
            put(entityReferenceInputStreamEntry.getKey(), entityReferenceInputStreamEntry.getValue());
        }

        for (Map.Entry<EntityReference, InputStream> entityReferenceInputStreamEntry : updatedEntities.entrySet())
        {
            put(entityReferenceInputStreamEntry.getKey(), entityReferenceInputStreamEntry.getValue());
        }

        for (EntityReference removedEntity : removedEntities)
        {
            remove(removedEntity);
        }
    }

    public void put(EntityReference entityReference, InputStream in) throws EntityStoreException
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Streams.copyStream(in, out, true);
            replicatedMap.put(entityReference.toString(), out.toByteArray());
        } catch (IOException e)
        {
            throw new EntityStoreException(e);
        }
    }

    public void get(EntityReference entityReference, OutputStream out) throws EntityNotFoundException
    {
        byte[] state = replicatedMap.get(entityReference.toString());
        if (state == null)
            throw new EntityNotFoundException(entityReference);
        try
        {
            out.write(state);
        } catch (IOException e)
        {
            throw new EntityStoreException(e);
        }
    }

    public void remove(EntityReference removedEntity) throws EntityNotFoundException
    {
        byte[] state = replicatedMap.remove(removedEntity.toString());
        if (state == null)
            throw new EntityNotFoundException(removedEntity);
    }

    public void visitMap(MapEntityStoreVisitor visitor)
    {
        for (byte[] bytes : replicatedMap.values())
        {
            visitor.visitEntity(new ByteArrayInputStream(bytes));
        }
    }
}