/*
 * Copyright 2010 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.reindexer;

import info.aduna.io.FileUtil;

import java.io.File;

import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Test;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.AbstractQi4jTest;

@SuppressWarnings( "PublicInnerClass" )
public class ReindexerTest
        extends AbstractQi4jTest
{

    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // JDBM EntityStore
        new JdbmEntityStoreAssembler( Visibility.module ).assemble( module );

        // Native Sesame EntityFinder
        new RdfNativeSesameStoreAssembler().assemble( module );

        // Reindexer
        module.services( ReindexerService.class );

        // Configuration
        ModuleAssembly config = module.layer().module( "config" );
        config.services( MemoryEntityStoreService.class );
        config.entities( JdbmConfiguration.class, NativeConfiguration.class, ReindexerConfiguration.class ).visibleIn( Visibility.layer );

        // Test entity
        module.entities( MyEntity.class );

    }

    private static final String TEST_NAME = "foo";

    public static interface MyEntity
            extends EntityComposite
    {

        Property<String> name();

    }

    @Test
    public void createDataAndWipeIndex()
            throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        EntityBuilder<MyEntity> eBuilder = uow.newEntityBuilder( MyEntity.class );
        MyEntity e = eBuilder.instance();
        e.name().set( TEST_NAME );
        e = eBuilder.newInstance();

        uow.complete();

        deleteIndexData(); // Here we wipe the index data on disk so that the next test can reindex
    }

    @Test
    public void reindexAndAssertData()
            throws UnitOfWorkCompletionException
    {
        serviceLocator.<ReindexerService>findService( ReindexerService.class ).get().reindex();

        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        QueryBuilder<MyEntity> qBuilder = queryBuilderFactory.newQueryBuilder( MyEntity.class );
        qBuilder = qBuilder.where( eq( templateFor( MyEntity.class ).name(), TEST_NAME ) );
        Query<MyEntity> q = qBuilder.newQuery( uow );

        assertEquals( 1, q.count() );
        assertEquals( TEST_NAME, q.iterator().next().name().get() );

        uow.complete();
    }

    @AfterClass
    @SuppressWarnings( "AssignmentReplaceableWithOperatorAssignment" )
    public static void afterClass()
            throws Exception
    {
        boolean success = true;
        success = deleteEntitiesData();
        success = success & deleteIndexData();
        if ( !success ) {
            throw new Exception( "Could not delete test data" );
        }

    }

    private static boolean deleteEntitiesData()
    {
        boolean success = true;
        File esDir = new File( "build/testdata/qi4j-entities" );
        if ( esDir.exists() ) {
            success = FileUtil.deltree( esDir );
        }
        return success;
    }

    private static boolean deleteIndexData()
    {
        boolean success = true;
        File rdfDir = new File( "build/testdata/qi4j-index" );
        if ( rdfDir.exists() ) {
            success = FileUtil.deltree( rdfDir );
        }
        return success;
    }

}
