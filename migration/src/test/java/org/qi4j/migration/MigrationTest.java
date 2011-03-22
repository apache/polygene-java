/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.migration.assembly.EntityMigrationOperation;
import org.qi4j.migration.assembly.MigrationBuilder;
import org.qi4j.migration.assembly.MigrationOperation;
import org.qi4j.spi.entitystore.BackupRestore;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.*;

/**
 * JAVADOC
 */
public class MigrationTest
        extends AbstractQi4jTest
{
   public void assemble(ModuleAssembly module)
           throws AssemblyException
   {
      new EntityTestAssembler().assemble(module);

      module.objects(MigrationEventLogger.class);
      module.importedServices(MigrationEventLogger.class).importedBy(NewObjectImporter.class);

      module.entities(TestEntity1_0.class,
              TestEntity1_1.class,
              TestEntity2_0.class,
              org.qi4j.migration.moved.TestEntity2_0.class);

      MigrationBuilder migration = new MigrationBuilder("1.0");
      migration.
              toVersion("1.1").
              renameEntity(TestEntity1_0.class.getName(), TestEntity1_1.class.getName()).
              atStartup(new CustomFixOperation("Fix for 1.1")).
              forEntities(TestEntity1_1.class.getName()).
              renameProperty("foo", "newFoo").
              renameManyAssociation("fooManyAssoc", "newFooManyAssoc").
              renameAssociation("fooAssoc", "newFooAssoc").
              end().
              toVersion("2.0").
              renameEntity(TestEntity1_1.class.getName(), TestEntity2_0.class.getName()).
              atStartup(new CustomFixOperation("Fix for 2.0, 1")).
              atStartup(new CustomFixOperation("Fix for 2.0, 2")).
              forEntities(TestEntity2_0.class.getName()).
              addProperty("bar", "Some value").
              removeProperty("newFoo", "Some value").
              custom(new CustomBarOperation()).
              end().
              toVersion("3.0").
              renamePackage("org.qi4j.migration", "org.qi4j.migration.moved").
              withEntities("TestEntity2_0").
              end();

      module.services(MigrationService.class).setMetaInfo(migration);
      module.entities(MigrationConfiguration.class);
      module.forMixin(MigrationConfiguration.class).declareDefaults().lastStartupVersion().set("1.0");
   }

   @Test
   public void testMigration()
           throws UnitOfWorkCompletionException, IOException
   {
      // Set up version 1
      String id;
      StringInputOutput data_v1 = new StringInputOutput();
      {
         SingletonAssembler v1 = new SingletonAssembler()
         {
            public void assemble(ModuleAssembly module)
                    throws AssemblyException
            {
               MigrationTest.this.assemble(module);
               module.layer().application().setVersion("1.0");
            }
         };

         UnitOfWork uow = v1.unitOfWorkFactory().newUnitOfWork();
         TestEntity1_0 entity = uow.newEntity(TestEntity1_0.class);
         entity.foo().set("Some value");
         entity.fooManyAssoc().add(entity);
         entity.fooAssoc().set(entity);
         id = entity.identity().get();
         uow.complete();

         BackupRestore backupRestore = (BackupRestore) v1.module()
                 .serviceFinder()
                 .findService(BackupRestore.class)
                 .get();
         backupRestore.backup().transferTo(data_v1);
      }

      // Set up version 1.1
      StringInputOutput data_v1_1 = new StringInputOutput();
      {
         SingletonAssembler v1_1 = new SingletonAssembler()
         {
            public void assemble(ModuleAssembly module)
                    throws AssemblyException
            {
               MigrationTest.this.assemble(module);
               module.layer().application().setVersion("1.1");
            }
         };

         BackupRestore testData = (BackupRestore) v1_1.serviceFinder().findService(BackupRestore.class).get();
         data_v1.transferTo(testData.restore());

         UnitOfWork uow = v1_1.unitOfWorkFactory().newUnitOfWork();
         TestEntity1_1 entity = uow.get(TestEntity1_1.class, id);
         assertThat("Property has been renamed", entity.newFoo().get(), CoreMatchers.equalTo("Some value"));
         assertThat("ManyAssociation has been renamed", entity.newFooManyAssoc().count(), CoreMatchers.equalTo(1));
         assertThat("Association has been renamed", entity.newFooAssoc().get(), CoreMatchers.equalTo(entity));
         uow.complete();

         testData.backup().transferTo(data_v1_1);
      }

      // Set up version 2.0
      {
         SingletonAssembler v2_0 = new SingletonAssembler()
         {
            public void assemble(ModuleAssembly module)
                    throws AssemblyException
            {
               MigrationTest.this.assemble(module);
               module.layer().application().setVersion("2.0");
            }
         };

         BackupRestore testData = (BackupRestore) v2_0.serviceFinder().findService(BackupRestore.class).get();

         // Test migration from 1.0 -> 2.0
         {
            data_v1.transferTo(testData.restore());
            UnitOfWork uow = v2_0.unitOfWorkFactory().newUnitOfWork();
            TestEntity2_0 entity = uow.get(TestEntity2_0.class, id);
            assertThat("Property has been created", entity.bar().get(), CoreMatchers.equalTo("Some value"));
            assertThat("Custom Property has been created", entity.customBar().get(), CoreMatchers.equalTo("Hello Some value"));
            assertThat("ManyAssociation has been renamed", entity.newFooManyAssoc().count(), CoreMatchers.equalTo(1));
            assertThat("Association has been renamed", entity.newFooAssoc().get(), CoreMatchers.equalTo(entity));
            uow.complete();
         }
      }

      // Set up version 3.0
      {
         SingletonAssembler v3_0 = new SingletonAssembler()
         {
            public void assemble(ModuleAssembly module)
                    throws AssemblyException
            {
               MigrationTest.this.assemble(module);
               module.layer().application().setVersion("3.0");
            }
         };

         BackupRestore testData = (BackupRestore) v3_0.serviceFinder().findService(BackupRestore.class).get();
         data_v1_1.transferTo(testData.restore());

         // Test migration from 1.0 -> 3.0
         {
            data_v1.transferTo(testData.restore());
            UnitOfWork uow = v3_0.unitOfWorkFactory().newUnitOfWork();
            org.qi4j.migration.moved.TestEntity2_0 entity = uow.get(org.qi4j.migration.moved.TestEntity2_0.class, id);
            uow.complete();
         }
      }
   }

   private static class CustomBarOperation
           implements EntityMigrationOperation
   {
      public boolean upgrade(JSONObject state, StateStore stateStore, Migrator migrator)
              throws JSONException
      {
         JSONObject properties = (JSONObject) state.get(MapEntityStore.JSONKeys.properties.name());

         return migrator.addProperty(state, "customBar", "Hello " + properties.getString("bar"));
      }

      public boolean downgrade(JSONObject state, StateStore stateStore, Migrator migrator)
              throws JSONException
      {
         return migrator.removeProperty(state, "customBar");
      }
   }

   private static class CustomFixOperation
           implements MigrationOperation
   {
      String msg;

      private CustomFixOperation(String msg)
      {
         this.msg = msg;
      }

      public void upgrade(StateStore stateStore, Migrator migrator)
              throws IOException
      {
         System.out.println(msg);
      }

      public void downgrade(StateStore stateStore, Migrator migrator)
              throws IOException
      {
         System.out.println(msg);
      }
   }

   private static class StringInputOutput
           implements Output<String, IOException>, Input<String, IOException>
   {
      final StringBuilder builder = new StringBuilder();

      @Override
      public <SenderThrowableType extends Throwable> void receiveFrom(Sender<? extends String, SenderThrowableType> sender) throws IOException, SenderThrowableType
      {
         sender.sendTo(new Receiver<String, IOException>()
         {
            public void receive(String item)
                    throws IOException
            {
               builder.append(item).append("\n");
            }
         });
      }

      @Override
      public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super String, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
      {
         output.receiveFrom(new Sender<String, IOException>()
         {
            @Override
            public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super String, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
            {
               BufferedReader reader = new BufferedReader(new StringReader(builder.toString()));
               String line;
               try
               {
                  while ((line = reader.readLine()) != null)
                     receiver.receive(line);
               } finally
               {
                  reader.close();
               }
            }
         });
      }

      @Override
      public String toString()
      {
         return builder.toString();
      }
   }
}
