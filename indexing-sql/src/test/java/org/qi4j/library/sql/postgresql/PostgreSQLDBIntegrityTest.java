/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

package org.qi4j.library.sql.postgresql;

import java.sql.Connection;

import junit.framework.Assert;

import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.sql.support.common.DBNames;
import org.qi4j.index.sql.support.common.GenericDatabaseExplorer;
import org.qi4j.index.sql.support.common.GenericDatabaseExplorer.DatabaseProcessorAdapter;
import org.qi4j.index.sql.support.postgresql.PostgreSQLAppStartup;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.ds.DataSourceService;
import org.qi4j.test.AbstractQi4jTest;
import org.sql.generation.api.vendor.PostgreSQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

/**
 * 
 * @author Stanislav Muhametsin
 */
public class PostgreSQLDBIntegrityTest extends AbstractQi4jTest
{

    public static interface TestEntity
        extends EntityComposite
    {
        @UseDefaults
        public Property<String> testString();

        @UseDefaults
        public Property<Integer> testInt();
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        SQLTestHelper.assembleWithMemoryEntityStore( module );
        module.entities( TestEntity.class );
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        if( this.serviceLocator != null )
        {
            SQLTestHelper.setUpTest( this.serviceLocator );
        }
    }

    @Override
    public void tearDown()
        throws Exception
    {
        SQLTestHelper.tearDownTest( unitOfWorkFactory, serviceLocator, getLog() );
        super.tearDown();
    }

    @Test
    public void createAndRemoveEntityAndVerifyNoExtraDataLeftInDB()
        throws Exception
    {
        UnitOfWork uow = this.unitOfWorkFactory.newUnitOfWork();
        TestEntity entity = uow.newEntity( TestEntity.class );
        uow.complete();

        uow = this.unitOfWorkFactory.newUnitOfWork();
        entity = uow.get( entity );
        SQLConfiguration config = uow.get( SQLConfiguration.class, SQLTestHelper.SQL_INDEXING_SERVICE_NAME );
        String schemaName = config.schemaName().get();
        if( schemaName == null )
        {
            schemaName = PostgreSQLAppStartup.DEFAULT_SCHEMA_NAME;
        }
        uow.remove( entity );
        uow.complete();

        Connection connection = ((DataSourceService) this.serviceLocator.findService( DataSourceService.class ).get())
            .getDataSource().getConnection();
        GenericDatabaseExplorer.visitDatabaseTables( connection, null, schemaName, null, new DatabaseProcessorAdapter()
        {

            @Override
            public void beginProcessRowInfo( String schemaNamee, String tableName, Object[] rowContents )
            {
                if( tableName.startsWith( DBNames.QNAME_TABLE_NAME_PREFIX )
                    || tableName.equals( DBNames.ALL_QNAMES_TABLE_NAME )
                    || tableName.equals( DBNames.ENTITY_TABLE_NAME ) )
                {
                    throw new RuntimeException( "Table: " + schemaNamee + "." + tableName );
                }
            }
        }, SQLVendorProvider.createVendor( PostgreSQLVendor.class ) );
    }

    @Test
    public void createAndModifyEntity()
        throws Exception
    {
        UnitOfWork uow = this.unitOfWorkFactory.newUnitOfWork();
        TestEntity entity = uow.newEntity( TestEntity.class );
        uow.complete();

        uow = this.unitOfWorkFactory.newUnitOfWork();
        entity = uow.get( entity );
        entity.testString().set( "NewTestString" );
        uow.complete();

        uow = this.unitOfWorkFactory.newUnitOfWork();
        entity = uow.get( entity );
        Assert.assertEquals( "New value did not store in indexing.", "NewTestString", entity.testString().get() );
        uow.discard();
    }

}
