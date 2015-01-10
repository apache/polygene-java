/*
 * Copyright (c) 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.elasticsearch;

import java.io.File;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.util.DelTreeAfter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

/**
 * ImmenseTermTest.
 * <p>
 * See <a href="https://ops4j1.jira.com/browse/QI-412">QI-412</a>.
 */
public class ImmenseTermTest
    extends AbstractQi4jTest
{
    private static final File DATA_DIR = new File( "build/tmp/immense-term-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

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
        config.values(SpatialConfiguration.Configuration.class,
                SpatialConfiguration.FinderConfiguration.class,
                SpatialConfiguration.IndexerConfiguration.class,
                SpatialConfiguration.IndexingMethod.class,
                SpatialConfiguration.ProjectionSupport.class).
                visibleIn(Visibility.application);
        new EntityTestAssembler().assemble( config );

        // EntityStore
        new EntityTestAssembler().assemble( module );

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
            withConfig( config, Visibility.layer ).
            assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
            withData( new File( DATA_DIR, "qi4j-data" ) ).
            withLog( new File( DATA_DIR, "qi4j-logs" ) ).
            withTemporary( new File( DATA_DIR, "qi4j-temp" ) );
        module.services( FileConfigurationService.class ).
            setMetaInfo( override );

        // Entities & Values
        module.entities( TestEntity.class, TestEntity2.class );
    }

    @Test
    public void testManyAssociation()
        throws Exception
    {
        long count = 10_000L;
        TestEntity testEntity;
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            testEntity = uow.newEntity( TestEntity.class );
            for( long i = 0; i < count; i++ )
            {
                TestEntity2 testEntity2 = module.currentUnitOfWork().newEntity( TestEntity2.class );
                testEntity2.property().set( "test" );
                testEntity.manyAssociation().add( testEntity2 );
            }
            uow.complete();
        }
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            testEntity = uow.get( testEntity );
            Query<TestEntity2> query = uow.newQuery(
                module.newQueryBuilder( TestEntity2.class ).where(
                    eq( templateFor( TestEntity2.class ).property(), "test" )
                )
            );
            assertThat( query.count(), is( count ) );
            assertThat( testEntity.manyAssociation().count(), is( (int) count ) );
        }
    }
}
