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

package org.apache.zest.index.elasticsearch;

import java.util.List;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.Queryable;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.templateFor;
import static org.apache.zest.test.util.Assume.assumeNoIbmJdk;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * ImmenseTermTest.
 * <p>
 * See <a href="https://ops4j1.jira.com/browse/QI-412">QI-412</a>.
 */
public class ImmenseTermTest
    extends AbstractZestTest
{
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    public interface TestEntity
        extends EntityComposite
    {
        @Optional
        Property<String> property();

        @Queryable( false )
        ManyAssociation<TestEntity2> manyAssociation();
    }

    public interface TestEntity2
        extends EntityComposite
    {
        @Optional
        Property<String> property();

        @Optional
        Property<List<Byte>> binaryProperty();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        // EntityStore
        new EntityTestAssembler().assemble( module );

        // Index/Query
        new ESFilesystemIndexQueryAssembler()
            .withConfig( config, Visibility.layer )
            .assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );

        // Entities & Values
        module.entities( TestEntity.class, TestEntity2.class );
    }

    @Test
    public void testManyAssociation()
        throws Exception
    {
        long count = 10_000L;
        TestEntity testEntity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = uow.newEntity( TestEntity.class );
            for( long i = 0; i < count; i++ )
            {
                TestEntity2 testEntity2 = unitOfWorkFactory.currentUnitOfWork().newEntity( TestEntity2.class );
                testEntity2.property().set( "test" );
                testEntity.manyAssociation().add( testEntity2 );
            }
            uow.complete();
        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            testEntity = uow.get( testEntity );
            Query<TestEntity2> query = uow.newQuery(
                queryBuilderFactory.newQueryBuilder( TestEntity2.class ).where(
                    eq( templateFor( TestEntity2.class ).property(), "test" )
                )
            );
            assertThat( query.count(), is( count ) );
            assertThat( testEntity.manyAssociation().count(), is( (int) count ) );
        }
    }
}
