package org.qi4j.entitystore.memory;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.util.Streams;
import org.qi4j.spi.entity.helpers.MapEntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityNotFoundException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.*;

/**
 * In-memory implementation of MapEntityStore.
 */
public class MemoryMapEntityStoreMixin
    implements MapEntityStore
{
    private final Map<EntityReference, byte[]> store;

    public MemoryMapEntityStoreMixin()
    {
        store = new HashMap<EntityReference, byte[]>();
    }

    public boolean contains(EntityReference entityReference)
    {
        return store.containsKey(entityReference);
    }

    public void get(EntityReference entityReference, OutputStream out) throws EntityNotFoundException
    {
        byte[] state = store.get(entityReference);
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
            store.put(entityReference, out.toByteArray());
        } catch (IOException e)
        {
            throw new EntityStoreException(e);
        }
    }

    public void remove(EntityReference removedEntity) throws EntityNotFoundException
    {
        byte[] state = store.remove(removedEntity);
        if (state == null)
            throw new EntityNotFoundException(removedEntity);
    }

    public void visitMap(MapEntityStoreVisitor visitor)
    {
        for (byte[] bytes : store.values())
        {
            visitor.visitEntity(new ByteArrayInputStream(bytes));
        }
    }
}