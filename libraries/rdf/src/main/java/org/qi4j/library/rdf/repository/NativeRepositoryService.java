/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.rdf.repository;

import java.io.File;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Availability;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.library.fileconfig.FileConfiguration;

@Mixins({NativeRepositoryService.NativeRepositoryMixin.class})
@Activators( NativeRepositoryService.Activator.class )
public interface NativeRepositoryService extends Repository, ServiceComposite, Availability
{
    @Override
    void initialize()
            throws RepositoryException;

    @Override
    void shutDown()
            throws RepositoryException;

    public static class Activator
            extends ActivatorAdapter<ServiceReference<NativeRepositoryService>>
    {

        @Override
        public void afterActivation( ServiceReference<NativeRepositoryService> activated )
                throws Exception
        {
            activated.get().initialize();
        }

        @Override
        public void beforePassivation( ServiceReference<NativeRepositoryService> passivating )
                throws Exception
        {
            passivating.get().shutDown();
        }

    }


    public static abstract class NativeRepositoryMixin
           implements NativeRepositoryService, ResetableRepository
   {
      @Optional
      @Service
      FileConfiguration fileConfiguration;

      @This
      private Configuration<NativeConfiguration> configuration;

      private SailRepository repo;
      private boolean isNotInitialized;

      public NativeRepositoryMixin()
      {
         isNotInitialized = true;
         repo = new SailRepository(new NativeStore());
      }

      @Override
      public void setDataDir(File dataDir)
      {
         repo.setDataDir(dataDir);
      }

      @Override
      public File getDataDir()
      {
         return repo.getDataDir();
      }

      @Override
      public void initialize()
              throws RepositoryException
      {
         String dataDir = configuration.get().dataDirectory().get();
         if (dataDir == null || "".equals(dataDir))
         {
            if (fileConfiguration != null)
            {
               dataDir = new File(fileConfiguration.dataDirectory(), configuration.get().identity().get()).getAbsolutePath();
            } else
            {
               String id = configuration.get().identity().get();
               if (id == null || "".equals(id))
               {
                  dataDir = "./rdf/repositories/qi4j";
               } else
               {
                  dataDir = "./rdf/repositories/" + id;
               }
            }
            configuration.get().dataDirectory().set(dataDir);
            configuration.save();
         }
         initializeRepository(new File(dataDir));
      }

      @Override
      public void shutDown()
              throws RepositoryException
      {
          repo.shutDown();
      }

      @Override
      public boolean isWritable()
              throws RepositoryException
      {
         return repo.isWritable();
      }

      @Override
      public RepositoryConnection getConnection()
              throws RepositoryException
      {
         if (isNotInitialized)
         {
            return null;
         }
         return repo.getConnection();
      }

      @Override
      public ValueFactory getValueFactory()
      {
         return repo.getValueFactory();
      }

      @Override
      public void discardEntireRepository()
              throws RepositoryException
      {
         File dataDir = repo.getDataDir();
         repo.shutDown();
         delete(dataDir);
         initializeRepository(dataDir);
      }

      private void delete(File dataDir)
      {
         File[] children = dataDir.listFiles();
         for (File child : children)
         {
            if (child.isDirectory())
            {
               delete(child);
            } else
            {
               //noinspection ResultOfMethodCallIgnored
               child.delete();
            }
         }
      }

      private void initializeRepository(File dataDir)
              throws RepositoryException
      {
         String tripleIndexes = configuration.get().tripleIndexes().get();
         if (tripleIndexes == null)
         {
            tripleIndexes = "";
            configuration.get().tripleIndexes().set(tripleIndexes);
         }
         boolean forceSync = configuration.get().forceSync().get();

         NativeStore store = (NativeStore) repo.getSail();
         store.setDataDir(dataDir);
         store.setTripleIndexes(tripleIndexes);
         store.setForceSync(forceSync);
         repo.initialize();
         isNotInitialized = false;
      }

       @Override
       public boolean isAvailable()
       {
           return !isNotInitialized;
       }
   }
}
