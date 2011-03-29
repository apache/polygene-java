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

import org.junit.After;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

import java.io.File;

/**
 * JAVADOC
 */
public class JdbmEntityStoreTest
        extends AbstractEntityStoreTest
{
   public void assemble(ModuleAssembly module)
           throws AssemblyException
   {
      super.assemble(module);
      module.services(FileConfiguration.class).instantiateOnStartup();
      module.services(JdbmEntityStoreService.class, UuidIdentityGeneratorService.class);

      ModuleAssembly config = module.layer().module("config");
      config.entities(JdbmConfiguration.class).visibleIn(Visibility.layer);
      config.services(MemoryEntityStoreService.class);
   }

   @Test
   @Override
   public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
           throws UnitOfWorkCompletionException
   {
      super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
   }

   @Override
   @After
   public void tearDown()
           throws Exception
   {
      super.tearDown();
      File dbFile = new File("qi4j.data.db");
      boolean success = true;
      if (dbFile.exists())
      {
         success = dbFile.delete();
      }

      File logFile = new File("qi4j.data.lg");
      if (logFile.exists())

      {
         success = success & logFile.delete();
      }
      if (!success)
      {
         throw new Exception("Could not delete test data");
      }
   }
}