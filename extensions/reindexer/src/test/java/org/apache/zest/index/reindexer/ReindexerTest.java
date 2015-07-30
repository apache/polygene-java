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
package org.apache.zest.index.reindexer;

import info.aduna.io.FileUtil;
import java.io.File;
import org.apache.tools.ant.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
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
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

@SuppressWarnings( "PublicInnerClass" )
public class ReindexerTest
        extends AbstractZestTest
{

    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // JDBM EntityStore
        new JdbmEntityStoreAssembler().assemble( module );

        // Native Sesame EntityFinder
        new RdfNativeSesameStoreAssembler().assemble( module );

        // Reindexer
        module.services( ReindexerService.class );

        // Configuration
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
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
    public void createDataWipeIndexReindexAndAssertData()
            throws UnitOfWorkCompletionException
    {
        File rdfDir = new File( System.getProperty( "user.dir" ), "build/testdata/zest-index" ).getAbsoluteFile();
        rdfDir.mkdirs();
        assertThat( rdfDir.exists(), is(true) );

        // ----> Create data and wipe index

        UnitOfWork uow = module.newUnitOfWork();

        EntityBuilder<MyEntity> eBuilder = uow.newEntityBuilder( MyEntity.class );
        MyEntity e = eBuilder.instance();
        e.name().set( TEST_NAME );
        e = eBuilder.newInstance();

        uow.complete();

        deleteIndexData(); // Wipe the index data on disk
        rdfDir.mkdirs();


        // ----> Reindex and assert data

        module.<ReindexerService>findService( ReindexerService.class ).get().reindex(); // Reindex

        uow = module.newUnitOfWork();

        QueryBuilder<MyEntity> qBuilder = module.newQueryBuilder( MyEntity.class );
        qBuilder = qBuilder.where( eq( templateFor( MyEntity.class ).name(), TEST_NAME ) );
        Query<MyEntity> q = uow.newQuery( qBuilder );

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
        File esDir = new File( System.getProperty( "user.dir" ), "build/testdata/zest-entities" ).getAbsoluteFile();
        if ( esDir.exists() ) {
            success = FileUtil.deltree( esDir );
        }
        return success;
    }

    private static boolean deleteIndexData()
    {
        boolean success = true;
        File rdfDir = new File( System.getProperty( "user.dir" ), "build/testdata/zest-index" ).getAbsoluteFile();
        if ( rdfDir.exists() ) {
            FileUtils.delete( rdfDir );
            success = FileUtil.deltree( rdfDir );
        }
        return success;
    }

}
