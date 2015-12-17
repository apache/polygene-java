/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.index.sql.postgresql;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.sql.assembly.PostgreSQLIndexQueryAssembler;
import org.apache.zest.index.sql.support.common.DBNames;
import org.apache.zest.index.sql.support.common.GenericDatabaseExplorer;
import org.apache.zest.index.sql.support.common.GenericDatabaseExplorer.DatabaseProcessorAdapter;
import org.apache.zest.index.sql.support.postgresql.PostgreSQLAppStartup;
import org.apache.zest.library.sql.common.SQLConfiguration;
import org.apache.zest.library.sql.common.SQLUtil;
import org.apache.zest.test.AbstractZestTest;
import org.sql.generation.api.vendor.PostgreSQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

import static org.apache.zest.test.util.Assume.assumeConnectivity;

public class PostgreSQLDBIntegrityTest
    extends AbstractZestTest
{
    @BeforeClass
    public static void beforePostgreSQLQueryTests()
    {
        assumeConnectivity( "localhost", 5432 );
    }

    public static interface TestEntity
        extends EntityComposite
    {
        @UseDefaults
        public Property<String> testString();

        @UseDefaults
        public Property<Integer> testInt();
    }

    @Override
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
        if( this.module != null )
        {
            SQLTestHelper.setUpTest( this.serviceFinder );
        }
    }

    @Test
    public void createAndRemoveEntityAndVerifyNoExtraDataLeftInDB()
        throws Exception
    {
        UnitOfWork uow = this.uowf.newUnitOfWork();
        TestEntity entity = uow.newEntity( TestEntity.class );
        uow.complete();

        uow = this.uowf.newUnitOfWork();
        entity = uow.get( entity );
        SQLConfiguration config = uow.get( SQLConfiguration.class, PostgreSQLIndexQueryAssembler.DEFAULT_IDENTITY );
        String schemaName = config.schemaName().get();
        if( schemaName == null )
        {
            schemaName = PostgreSQLAppStartup.DEFAULT_SCHEMA_NAME;
        }
        uow.remove( entity );
        uow.complete();

        Connection connection = this.serviceFinder.findService( DataSource.class ).get().getConnection();
        try
        {
            GenericDatabaseExplorer.visitDatabaseTables(
                connection, null, schemaName, null,
                new DatabaseProcessorAdapter()
            {
                @Override
                public void beginProcessRowInfo( String schemaNamee, String tableName, Object[] rowContents )
                {
                    if( ( tableName.startsWith( DBNames.QNAME_TABLE_NAME_PREFIX )
                          && ( tableName.equals( DBNames.QNAME_TABLE_NAME_PREFIX + 0 )
                               || tableName.equals( DBNames.QNAME_TABLE_NAME_PREFIX + 1 ) ) )
                        || tableName.equals( DBNames.ALL_QNAMES_TABLE_NAME )
                        || tableName.equals( DBNames.ENTITY_TABLE_NAME ) )
                    {
                        throw new RuntimeException( "Table: " + schemaNamee + "." + tableName );
                    }
                }
                },
                SQLVendorProvider.createVendor( PostgreSQLVendor.class ) );
        }
        finally
        {
            SQLUtil.closeQuietly( connection );
        }
    }

    @Test
    public void createAndModifyEntity()
        throws Exception
    {
        UnitOfWork uow = this.uowf.newUnitOfWork();
        TestEntity entity = uow.newEntity( TestEntity.class );
        uow.complete();

        uow = this.uowf.newUnitOfWork();
        entity = uow.get( entity );
        entity.testString().set( "NewTestString" );
        uow.complete();

        uow = this.uowf.newUnitOfWork();
        entity = uow.get( entity );
        Assert.assertEquals( "New value did not store in indexing.", "NewTestString", entity
            .testString().get() );
        uow.discard();
    }
}
