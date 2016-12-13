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
package org.apache.zest.index.reindexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.jdbm.JdbmConfiguration;
import org.apache.zest.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.apache.zest.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.zest.library.rdf.repository.NativeConfiguration;
import org.apache.zest.test.AbstractPolygeneTest;
import org.apache.zest.test.EntityTestAssembler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.templateFor;
import static org.junit.Assert.assertEquals;

public class ReindexerTest
    extends AbstractPolygeneTest
{
    private static final String ENTITIES_DIR = "zest-entities";
    private static final String INDEX_DIR = "zest-index";

    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // JDBM EntityStore
        new JdbmEntityStoreAssembler().assemble( module );

        // Native Sesame EntityFinder
        new RdfNativeSesameStoreAssembler().assemble( module );

        // Reindexer
        // START SNIPPET: assembly
        module.services( ReindexerService.class );
        // END SNIPPET: assembly

        // Configuration
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        config.entities( JdbmConfiguration.class, NativeConfiguration.class, ReindexerConfiguration.class )
              .visibleIn( Visibility.layer );
        config.forMixin( JdbmConfiguration.class ).declareDefaults()
              .file().set( new File( tmpDir.getRoot(), ENTITIES_DIR ).getAbsolutePath() );
        config.forMixin( NativeConfiguration.class ).declareDefaults()
              .dataDirectory().set( new File( tmpDir.getRoot(), INDEX_DIR ).getAbsolutePath() );

        // Test entity
        module.entities( MyEntity.class );
    }

    private static final String TEST_NAME = "foo";

    public interface MyEntity extends EntityComposite
    {

        Property<String> name();
    }

    @Test
    public void createDataWipeIndexReindexAndAssertData()
        throws UnitOfWorkCompletionException, IOException
    {

        // ----> Create data and wipe index

        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        EntityBuilder<MyEntity> eBuilder = uow.newEntityBuilder( MyEntity.class );
        MyEntity e = eBuilder.instance();
        e.name().set( TEST_NAME );
        e = eBuilder.newInstance();

        uow.complete();

        // Wipe the index data on disk
        try( Stream<Path> files = Files.walk( new File( tmpDir.getRoot(), INDEX_DIR ).getAbsoluteFile().toPath() ) )
        {
            files.map( Path::toFile ).forEach( File::delete );
        }


        // ----> Reindex and assert data

        // START SNIPPET: usage
        Reindexer reindexer = serviceFinder.findService( Reindexer.class ).get();
        reindexer.reindex();
        // END SNIPPET: usage

        uow = unitOfWorkFactory.newUnitOfWork();

        QueryBuilder<MyEntity> qBuilder = queryBuilderFactory.newQueryBuilder( MyEntity.class );
        qBuilder = qBuilder.where( eq( templateFor( MyEntity.class ).name(), TEST_NAME ) );
        Query<MyEntity> q = uow.newQuery( qBuilder );

        assertEquals( 1, q.count() );
        assertEquals( TEST_NAME, q.iterator().next().name().get() );

        uow.complete();
    }
}
