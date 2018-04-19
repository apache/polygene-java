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
package org.apache.polygene.index.sql.postgresql;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.sql.assembly.PostgreSQLIndexQueryAssembler;
import org.apache.polygene.index.sql.support.common.DBNames;
import org.apache.polygene.index.sql.support.common.GenericDatabaseExplorer;
import org.apache.polygene.index.sql.support.common.GenericDatabaseExplorer.DatabaseProcessorAdapter;
import org.apache.polygene.library.sql.common.SQLConfiguration;
import org.apache.polygene.library.sql.common.SQLUtil;
import org.apache.polygene.library.sql.generator.vendor.PostgreSQLVendor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendorProvider;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@Docker( image = "org.apache.polygene:org.apache.polygene.internal.docker-postgres",
         ports = @Port( exposed = 8801, inner = 5432 ),
         waitFor = @WaitFor( value = "PostgreSQL init process complete; ready for start up.", timeoutInMillis = 30000 ),
         newForEachCase = false
)
public class PostgreSQLDBIntegrityTest
    extends AbstractPolygeneTest
{
    public interface TestEntity
        extends EntityComposite
    {
        @UseDefaults
        Property<String> testString();

        @UseDefaults
        Property<Integer> testInt();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        SQLTestHelper.sleep();
        String host = "localhost";
        int port = 8801;
        SQLTestHelper.assembleWithMemoryEntityStore( module, host, port );
        module.entities( TestEntity.class );
    }

    @Override
    @BeforeEach
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
        UnitOfWork uow = this.unitOfWorkFactory.newUnitOfWork();
        TestEntity entity = uow.newEntity( TestEntity.class );
        uow.complete();

        uow = this.unitOfWorkFactory.newUnitOfWork();
        entity = uow.get( entity );
        SQLConfiguration config = uow.get( SQLConfiguration.class, PostgreSQLIndexQueryAssembler.DEFAULT_IDENTITY );
        String schemaName = config.schemaName().get().toLowerCase();
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
        UnitOfWork uow = this.unitOfWorkFactory.newUnitOfWork();
        TestEntity entity = uow.newEntity( TestEntity.class );
        uow.complete();

        uow = this.unitOfWorkFactory.newUnitOfWork();
        entity = uow.get( entity );
        entity.testString().set( "NewTestString" );
        uow.complete();

        uow = this.unitOfWorkFactory.newUnitOfWork();
        entity = uow.get( entity );
        assertThat( "New value did not store in indexing.", entity.testString().get(), equalTo( "NewTestString" ) );
        uow.discard();
    }
}
