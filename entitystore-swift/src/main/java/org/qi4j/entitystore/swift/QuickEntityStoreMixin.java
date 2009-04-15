/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.swift;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.serialization.FastObjectInputStream;
import org.qi4j.spi.serialization.FastObjectOutputStream;

public class QuickEntityStoreMixin
        implements EntityStore, Activatable
{
    private
    @This
    ReadWriteLock lock;
    @Uses
    private ServiceDescriptor descriptor;
    @This
    private Configuration<QuickConfiguration> configuration;
    private RecordManager recordManager;

    public QuickEntityStoreMixin()
    {
    }

    public void activate()
            throws Exception
    {
        QuickConfiguration conf = configuration.configuration();
        String storage = conf.storageDirectory().get();
        File storageDir;
        storageDir = new File(storage);
        Boolean recover = conf.recover().get();
        if (recover == null)
        {
            recover = Boolean.TRUE;
        }
        recordManager = new RecordManager(storageDir, recover);
    }

    public void passivate()
            throws Exception
    {
        recordManager.close();
    }

    public EntityState newEntityState(EntityReference reference) throws EntityStoreException
    {
        return new DefaultEntityState(reference);
    }

    public EntityState getEntityState(EntityReference reference) throws EntityStoreException
    {
        try
        {

            try
            {
                SerializableState serializableState = loadSerializableState(reference);
                if (serializableState == null)
                {
                    throw new EntityNotFoundException(reference);
                }

                return new DefaultEntityState(serializableState.version(),
                        serializableState.lastModified(),
                        reference,
                        EntityStatus.LOADED,
                        serializableState.entityTypeReferences(),
                        serializableState.properties(),
                        serializableState.associations(),
                        serializableState.manyAssociations());
            }
            catch (ClassNotFoundException e)
            {
                throw new EntityStoreException(e);
            }
        }
        catch (IOException e)
        {
            throw new EntityStoreException(e);
        }
    }

    public StateCommitter prepare(Iterable<EntityState> newStates, Iterable<EntityState> updatedStates, Iterable<EntityReference> removedStates)
            throws EntityStoreException
    {
        boolean turbo = configuration.configuration().turboMode().get();
        lock.writeLock().lock();

        long lastModified = System.currentTimeMillis();
        try
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            storeNewStates(newStates, turbo, lastModified, bout);
            storeLoadedStates(updatedStates, turbo, lastModified, bout);
            removeStates(removedStates);
        }
        catch (Throwable e)
        {
            try
            {
                recordManager.discard();
            }
            catch (IOException e1)
            {
                throw new EntityStoreException("Problem with underlying storage system.");
            }
            lock.writeLock().unlock();
            if (e instanceof EntityStoreException)
            {
                throw (EntityStoreException) e;
            } else
            {
                throw new EntityStoreException(e);
            }
        }

        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    recordManager.commit();
                }
                catch (IOException e1)
                {
                    throw new EntityStoreException("Problem with underlying storage system.");
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }

            public void cancel()
            {
                try
                {
                    recordManager.discard();
                }
                catch (IOException e1)
                {
                    throw new EntityStoreException("Problem with underlying storage system.");
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }
        };
    }

    public void visitEntityStates(EntityStateVisitor visitor)
    {
        final Iterator<EntityReference> iterator = recordManager.iterator();

        while (iterator.hasNext())
        {
            try
            {
                EntityReference reference = iterator.next();
                SerializableState serializableState = loadSerializableState(reference);
                if (serializableState == null)
                {
                    throw new EntityNotFoundException(reference);
                }

                visitor.visitEntityState(new DefaultEntityState(serializableState.version(),
                        serializableState.lastModified(),
                        serializableState.identity(),
                        EntityStatus.LOADED,
                        serializableState.entityTypeReferences(),
                        serializableState.properties(),
                        serializableState.associations(),
                        serializableState.manyAssociations()));
            }
            catch (Exception e)
            {
                throw new EntityStoreException(e);
            }
        }
    }


    private SerializableState loadSerializableState(EntityReference reference)
            throws IOException, ClassNotFoundException
    {
        DataBlock data = recordManager.readData(reference);

        if (data == null)
        {
            return null;
        }
        byte[] serializedState = data.data;

        ByteArrayInputStream bin = new ByteArrayInputStream(serializedState);
        ObjectInputStream oin = new FastObjectInputStream(bin, configuration.configuration().turboMode().get());
        return (SerializableState) oin.readObject();
    }

    private void storeNewStates(Iterable<EntityState> newStates, boolean turbo, long lastModified, ByteArrayOutputStream bout)
            throws IOException
    {
        for (EntityState entityState : newStates)
        {
            DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
            SerializableState state = new SerializableState(entityState.identity(),
                    entityState.version(),
                    lastModified,
                    entityStateInstance.entityTypeReferences(),
                    entityStateInstance.getProperties(),
                    entityStateInstance.getAssociations(),
                    entityStateInstance.getManyAssociations());
            ObjectOutputStream out = new FastObjectOutputStream(bout, turbo);
            out.writeObject(state);
            out.close();
            byte[] stateArray = bout.toByteArray();
            DataBlock data = new DataBlock(entityState.identity(), stateArray, entityState.version(), 1);
            recordManager.putData(data);
            bout.reset();
        }
    }

    private void storeLoadedStates(Iterable<EntityState> loadedStates, boolean turbo, long lastModified, ByteArrayOutputStream bout)
            throws IOException
    {
        for (EntityState entityState : loadedStates)
        {
            DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;

            if (entityStateInstance.isModified())
            {
                long newVersion = entityState.version() + 1;
                SerializableState state = new SerializableState(entityState.identity(),
                        newVersion,
                        lastModified,
                        entityStateInstance.entityTypeReferences(),
                        entityStateInstance.getProperties(),
                        entityStateInstance.getAssociations(),
                        entityStateInstance.getManyAssociations());
                ObjectOutputStream out = new FastObjectOutputStream(bout, turbo);
                out.writeObject(state);
                out.close();
                byte[] stateArray = bout.toByteArray();
                bout.reset();
                DataBlock data = new DataBlock(entityState.identity(), stateArray, newVersion, 1);
                recordManager.putData(data);
            }
        }
    }

    private void removeStates(Iterable<EntityReference> removedStates)
            throws IOException
    {
        for (EntityReference removedState : removedStates)
        {
            recordManager.deleteData(removedState);
        }
    }
}
