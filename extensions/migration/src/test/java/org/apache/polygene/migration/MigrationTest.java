/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.migration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import javax.json.JsonObject;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.service.importer.NewObjectImporter;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.migration.assembly.EntityMigrationOperation;
import org.apache.polygene.migration.assembly.MigrationBuilder;
import org.apache.polygene.migration.assembly.MigrationContext;
import org.apache.polygene.migration.assembly.MigrationOperation;
import org.apache.polygene.spi.entitystore.BackupRestore;
import org.apache.polygene.spi.entitystore.helpers.JSONKeys;
import org.apache.polygene.spi.entitystore.helpers.StateStore;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * JAVADOC
 */
public class MigrationTest
    extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.objects( MigrationEventLogger.class );
        module.importedServices( MigrationEventLogger.class ).importedBy( NewObjectImporter.class );

        module.entities( TestEntity1_0.class,
                         TestEntity1_1.class,
                         TestEntity2_0.class,
                         org.apache.polygene.migration.moved.TestEntity2_0.class );

        MigrationBuilder migration = new MigrationBuilder( "1.0" );
        migration.
            toVersion( "1.1" ).
            renameEntity( TestEntity1_0.class.getName(), TestEntity1_1.class.getName() ).
            atStartup( new CustomFixOperation( "Fix for 1.1" ) ).
            forEntities( TestEntity1_1.class.getName() ).
            renameProperty( "foo", "newFoo" ).
            renameManyAssociation( "fooManyAssoc", "newFooManyAssoc" ).
            renameAssociation( "fooAssoc", "newFooAssoc" ).
            end().
            toVersion( "2.0" ).
            renameEntity( TestEntity1_1.class.getName(), TestEntity2_0.class.getName() ).
            atStartup( new CustomFixOperation( "Fix for 2.0, 1" ) ).
            atStartup( new CustomFixOperation( "Fix for 2.0, 2" ) ).
            forEntities( TestEntity2_0.class.getName() ).
            addProperty( "bar", "Some value" ).
            removeProperty( "newFoo", "Some value" ).
            custom( new CustomBarOperation() ).
            end().
            toVersion( "3.0" ).
            renamePackage( "org.apache.polygene.migration", "org.apache.polygene.migration.moved" ).
            withEntities( "TestEntity2_0" ).
            end();

        module.services( MigrationService.class ).setMetaInfo( migration );
        module.entities( MigrationConfiguration.class );
        module.forMixin( MigrationConfiguration.class ).declareDefaults().lastStartupVersion().set( "1.0" );
    }

    @Test
    public void testMigration()
        throws UnitOfWorkCompletionException, IOException, ActivationException, AssemblyException
    {
        Identity id;
        // Set up version 1
        List<String> data_v1;
        {
            SingletonAssembler v1 = new SingletonAssembler(
                moduleAssembly -> {
                    MigrationTest.this.assemble( moduleAssembly );
                    moduleAssembly.layer().application().setVersion( "1.0" );
                }
            );

            UnitOfWork uow = v1.module().unitOfWorkFactory().newUnitOfWork();
            TestEntity1_0 entity = uow.newEntity( TestEntity1_0.class );
            entity.foo().set( "Some value" );
            entity.fooManyAssoc().add( entity );
            entity.fooAssoc().set( entity );
            id = entity.identity().get();
            uow.complete();

            BackupRestore backupRestore = v1.module().findService( BackupRestore.class ).get();
            try( Stream<String> backup = backupRestore.backup() )
            {
                data_v1 = backup.collect( toList() );
            }
        }

        // Set up version 1.1
        List<String> data_v1_1;
        {
            SingletonAssembler v1_1 = new SingletonAssembler(
                moduleAssembly -> {
                    MigrationTest.this.assemble( moduleAssembly );
                    moduleAssembly.layer().application().setVersion( "1.1" );
                }
            );

            BackupRestore testData = v1_1.module().findService( BackupRestore.class ).get();
            testData.restore( data_v1.stream() );

            UnitOfWork uow = v1_1.module().unitOfWorkFactory().newUnitOfWork();
            TestEntity1_1 entity = uow.get( TestEntity1_1.class, id );
            assertThat( "Property has been renamed", entity.newFoo().get(), equalTo( "Some value" ) );
            assertThat( "ManyAssociation has been renamed", entity.newFooManyAssoc().count(), equalTo( 1 ) );
            assertThat( "Association has been renamed", entity.newFooAssoc().get(), equalTo( entity ) );
            uow.complete();

            try( Stream<String> backup = testData.backup() )
            {
                data_v1_1 = backup.collect( toList() );
            }
        }

        // Set up version 2.0
        {
            SingletonAssembler v2_0 = new SingletonAssembler(
                moduleAssembly -> {
                    MigrationTest.this.assemble( moduleAssembly );
                    moduleAssembly.layer().application().setVersion( "2.0" );
                }
            );

            BackupRestore testData = v2_0.module().findService( BackupRestore.class ).get();

            // Test migration from 1.0 -> 2.0
            {
                testData.restore( data_v1.stream() );
                UnitOfWork uow = v2_0.module().unitOfWorkFactory().newUnitOfWork();
                TestEntity2_0 entity = uow.get( TestEntity2_0.class, id );
                assertThat( "Property has been created", entity.bar().get(), equalTo( "Some value" ) );
                assertThat( "Custom Property has been created", entity.customBar().get(), equalTo( "Hello Some value" ) );
                assertThat( "ManyAssociation has been renamed", entity.newFooManyAssoc().count(), equalTo( 1 ) );
                assertThat( "Association has been renamed", entity.newFooAssoc().get(), equalTo( entity ) );
                uow.complete();
            }
        }

        // Set up version 3.0
        {
            SingletonAssembler v3_0 = new SingletonAssembler(
                moduleAssembly -> {
                    MigrationTest.this.assemble( moduleAssembly );
                    moduleAssembly.layer().application().setVersion( "3.0" );
                }
            );

            BackupRestore testData = v3_0.module().findService( BackupRestore.class ).get();
            testData.restore( data_v1_1.stream() );

            // Test migration from 1.0 -> 3.0
            {
                testData.restore( data_v1.stream() );
                UnitOfWork uow = v3_0.module().unitOfWorkFactory().newUnitOfWork();
                org.apache.polygene.migration.moved.TestEntity2_0 entity = uow.get( org.apache.polygene.migration.moved.TestEntity2_0.class, id );
                uow.complete();
            }
        }
    }

    private static class CustomBarOperation
        implements EntityMigrationOperation
    {
        @Override
        public JsonObject upgrade( MigrationContext context, JsonObject state, StateStore store, Migrator migrator )
        {
            JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
            return migrator.addProperty( context, state, "customBar", "Hello " + valueState.getString( "bar" ) );
        }

        @Override
        public JsonObject downgrade( MigrationContext context, JsonObject state, StateStore store, Migrator migrator )
        {
            return migrator.removeProperty( context, state, "customBar" );
        }
    }

    private static class CustomFixOperation
        implements MigrationOperation
    {
        String msg;

        private CustomFixOperation( String msg )
        {
            this.msg = msg;
        }

        @Override
        public void upgrade( StateStore stateStore, Migrator migrator )
            throws IOException
        {
            System.out.println( msg );
        }

        @Override
        public void downgrade( StateStore stateStore, Migrator migrator )
            throws IOException
        {
            System.out.println( msg );
        }
    }
}
