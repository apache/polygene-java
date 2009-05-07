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
package org.qi4j.entitystore.jdbm;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import jdbm.helper.*;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.helpers.MapEntityStore;
import org.qi4j.spi.service.ServiceDescriptor;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * JDBM implementation of SerializationStore
 */
public class JdbmEntityStoreMixin
        implements Activatable, MapEntityStore
{
    private
    @This
    ReadWriteLock lock;
    private
    @This
    Configuration<JdbmConfiguration> config;
    private
    @Uses
    ServiceDescriptor descriptor;

    private RecordManager recordManager;
    private BTree index;
    private Serializer serializer;
    private long registryId;

    // Activatable implementation
    public void activate()
            throws Exception
    {
        File dataFile = new File(config.configuration().file().get());
        System.out.println("JDBM store:" + dataFile.getAbsolutePath());
        File directory = dataFile.getAbsoluteFile().getParentFile();
        directory.mkdirs();
        String name = dataFile.getAbsolutePath();
        Properties properties;
        try
        {
            properties = getProperties(directory);
        }
        catch (IOException e)
        {
            throw new EntityStoreException("Unable to read properties from " + directory + "/qi4j.properties", e);
        }
        recordManager = RecordManagerFactory.createRecordManager(name, properties);
        serializer = new ByteArraySerializer();

        initializeIndex();
    }

    public void passivate()
            throws Exception
    {
        recordManager.close();
    }

    public boolean contains( EntityReference entityReference, Usecase usecase, MetaInfo unitofwork ) throws EntityStoreException
    {
        try
        {
            Long stateIndex = getStateIndex(entityReference.toString());
            return stateIndex != null;
        } catch (IOException e)
        {
            throw new EntityStoreException(e);
        }
    }

    public InputStream get( EntityReference entityReference, Usecase usecase, MetaInfo unitOfWork ) throws EntityStoreException
    {
        try
        {
            Long stateIndex = getStateIndex(entityReference.identity());

            if (stateIndex == null)
            {
                throw new EntityNotFoundException(entityReference);
            }

            byte[] serializedState = (byte[]) recordManager.fetch(stateIndex, serializer);

            if (serializedState == null)
            {
                throw new EntityNotFoundException(entityReference);
            }

            return new ByteArrayInputStream(serializedState);
        } catch (IOException e)
        {
            throw new EntityStoreException(e);
        }
    }

    public void applyChanges( MapChanges changes, Usecase usecase, MetaInfo unitOfWork ) throws IOException
    {
        try
        {
            changes.visitMap( new MapChanger()
            {
                public OutputStream newEntity( final EntityReference ref ) throws IOException
                {
                    return new ByteArrayOutputStream(1000)
                    {
                        @Override public void close() throws IOException
                        {
                            super.close();

                            byte[] stateArray = toByteArray();
                            long stateIndex = recordManager.insert(stateArray, serializer);
                            String indexKey = ref.toString();
                            index.insert(indexKey.getBytes("UTF-8"), stateIndex, false);
                        }
                    };
                }

                public OutputStream updateEntity( final EntityReference ref ) throws IOException
                {
                    return new ByteArrayOutputStream(1000)
                    {
                        @Override public void close() throws IOException
                        {
                            super.close();

                            Long stateIndex = getStateIndex(ref.toString());
                            byte[] stateArray = toByteArray();
                            recordManager.update(stateIndex, stateArray, serializer);
                        }
                    };
                }

                public void removeEntity( EntityReference ref ) throws EntityNotFoundException
                {
                    try
                    {
                        Long stateIndex = getStateIndex(ref.toString());
                        recordManager.delete(stateIndex);
                        index.remove(ref.toString().getBytes("UTF-8"));
                    }
                    catch( IOException e )
                    {
                        throw new EntityStoreException(e);
                    }
                }
            }, usecase, unitOfWork);

            recordManager.commit();
        } catch (Exception e)
        {
            recordManager.rollback();
            if (e instanceof IOException)
                throw (IOException) e;
            else if (e instanceof EntityStoreException)
                throw (EntityStoreException) e;
            else
                throw (IOException) new IOException().initCause( e );
        }

    }


    public void visitMap(MapEntityStoreVisitor visitor, Usecase usecase, MetaInfo unitOfWorkMetaInfo)
    {
        try
        {
            final TupleBrowser browser = index.browse();
            final Tuple tuple = new Tuple();

            while (browser.getNext(tuple))
            {
                String id = new String((byte[]) tuple.getKey(), "UTF-8");

                Long stateIndex = getStateIndex(id);

                if (stateIndex == null)
                {
                    throw new EntityNotFoundException(new EntityReference(id));
                }

                byte[] serializedState = (byte[]) recordManager.fetch(stateIndex, serializer);

                visitor.visitEntity(new ByteArrayInputStream(serializedState));
            }
        }
        catch (IOException e)
        {
            throw new EntityStoreException(e);
        }
    }

    private Properties getProperties(File directory)
            throws IOException
    {
        Properties properties = new Properties();
        File propertiesFile = new File(directory, "qi4j.properties");
        if (propertiesFile.exists())
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(propertiesFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                properties.load(bis);
            }
            finally
            {
                if (fis != null)
                {
                    fis.close();
                }
            }
        }
        return properties;
    }

    private Long getStateIndex(String identity)
            throws IOException
    {
        Long stateIndex = (Long) index.find(identity.getBytes("UTF-8"));
        return stateIndex;
    }

    private void initializeIndex()
            throws IOException
    {
        long recid = recordManager.getNamedObject("index");
        if (recid != 0)
        {
            System.out.println("Using existing index");
            index = BTree.load(recordManager, recid);
        } else
        {
            System.out.println("Creating new index");
            index = BTree.createInstance(recordManager, new ByteArrayComparator(), new ByteArraySerializer(), new LongSerializer(), 16);
            recordManager.setNamedObject("index", index.getRecid());
        }
    }
}