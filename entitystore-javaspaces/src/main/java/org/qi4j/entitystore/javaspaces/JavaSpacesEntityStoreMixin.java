/*  Copyright 2008 Jan Kronquist.
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
package org.qi4j.entitystore.javaspaces;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.Streams;
import org.qi4j.library.spaces.Space;
import org.qi4j.library.spaces.SpaceException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.helpers.MapEntityStore;
import org.qi4j.spi.service.ServiceDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Java Spaces implementation of EntityStore.
 */
public class JavaSpacesEntityStoreMixin
        implements MapEntityStore
{
    @Uses
    private ServiceDescriptor descriptor;
    @Service
    private Space space;

    public boolean contains(EntityReference entityReference) throws EntityStoreException
    {
        StorageEntry entry = (StorageEntry) space.readIfExists(entityReference.toString());
        return entry != null;
    }

    public void get(EntityReference entityReference, OutputStream out) throws EntityStoreException
    {
        try
        {
            StorageEntry entry = (StorageEntry) space.readIfExists(entityReference.toString());
            byte[] serializedState = entry.getData();
            if (serializedState == null)
            {
                throw new EntityNotFoundException(entityReference);
            }
            out.write(serializedState);
        }
        catch (Exception e)
        {
            throw new EntityStoreException(e);
        }
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
            byte[] stateArray = out.toByteArray();
            StorageEntry data = new StorageEntry(entityReference);
            data.setData(stateArray);
            space.write(entityReference.toString(), data);
        }
        catch (Exception e)
        {
            throw new EntityStoreException("Unable to write state");
        }
    }

    public void remove(EntityReference removedEntity) throws EntityStoreException
    {
        try
        {
            space.takeIfExists(removedEntity.toString());
        }
        catch (SpaceException e)
        {
            throw new EntityStoreException(e);
        }
    }

    public void visitMap(MapEntityStoreVisitor visitor)
    {
        // TODO 
    }

    private class StorageEntry
            implements Serializable
    {
        public EntityReference reference;
        private byte[] data;

        public StorageEntry(EntityReference reference)
        {
            this.reference = reference;
        }

        public byte[] getData()
        {
            return data;
        }

        public void setData(byte[] data)
        {
            this.data = data;
        }

    }
}
