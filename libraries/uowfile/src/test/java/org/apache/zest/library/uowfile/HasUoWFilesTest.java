/*
 * Copyright 2011-2014 Paul Merlin.
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
package org.apache.zest.library.uowfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkRetry;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.io.Inputs;
import org.apache.zest.io.Outputs;
import org.apache.zest.library.fileconfig.FileConfigurationAssembler;
import org.apache.zest.library.uowfile.bootstrap.UoWFileAssembler;
import org.apache.zest.library.uowfile.internal.ConcurrentUoWFileModificationException;
import org.apache.zest.library.uowfile.plural.HasUoWFilesLifecycle;
import org.apache.zest.library.uowfile.plural.UoWFilesLocator;
import org.apache.zest.test.EntityTestAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HasUoWFilesTest
    extends AbstractUoWFileTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( HasUoWFilesTest.class );
    private static final URL CREATION_CONTENT_URL = HasUoWFilesTest.class.getResource( "creation.txt" );
    private static final URL MODIFICATION_CONTENT_URL = HasUoWFilesTest.class.getResource( "modification.txt" );

    // START SNIPPET: uowfile
    public enum MyEnum
    {
        fileOne, fileTwo
    }

    // START SNIPPET: entity
    public interface TestedEntity
        extends HasUoWFilesLifecycle<MyEnum> // END SNIPPET: entity
        , Identity
    // START SNIPPET: entity
    {
        Property<String> name();
    }
    // END SNIPPET: entity
    // END SNIPPET: uowfile

    // START SNIPPET: locator
    public static abstract class TestedFilesLocatorMixin
        implements UoWFilesLocator<MyEnum>
    {
        @This
        private Identity meAsIdentity;

        @Override
        public Iterable<File> locateAttachedFiles()
        {
            List<File> list = new ArrayList<>();
            for( MyEnum eachValue : MyEnum.values() )
            {
                list.add( new File( baseTestDir, meAsIdentity.identity().get() + "." + eachValue.name() ) );
            }
            return list;
        }

        @Override
        public File locateAttachedFile( MyEnum key )
        {
            return new File( baseTestDir, meAsIdentity.identity().get() + "." + key.name() );
        }
    }
    // END SNIPPET: locator

    @Mixins( TestServiceMixin.class )
    @Concerns( UnitOfWorkConcern.class )
    public interface TestService
    {
        void modifyFile( String entityId )
            throws IOException;

        @UnitOfWorkPropagation
        @UnitOfWorkRetry
        void modifyFileWithRetry( String entityId, long sleepBefore, long sleepAfter )
            throws IOException;
    }

    public static class TestServiceMixin
        implements TestService
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public void modifyFile( String entityId )
            throws IOException
        {
            modifyFileImmediatly( entityId );
        }

        @Override
        public void modifyFileWithRetry( String entityId, long sleepBefore, long sleepAfter )
            throws IOException
        {
            LOGGER.info( "Waiting " + sleepBefore + "ms before file modification" );
            if( sleepBefore > 0 )
            {
                try
                {
                    Thread.sleep( sleepBefore );
                }
                catch( InterruptedException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
            modifyFileImmediatly( entityId );
            LOGGER.info( "Waiting " + sleepAfter + "ms after file modification" );
            if( sleepAfter > 0 )
            {
                try
                {
                    Thread.sleep( sleepAfter );
                }
                catch( InterruptedException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }

        private void modifyFileImmediatly( String entityId )
            throws IOException
        {
            TestedEntity entity = uowf.currentUnitOfWork().get( TestedEntity.class, entityId );
            // START SNIPPET: api
            File attachedFileTwo = entity.attachedFile( MyEnum.fileTwo );
            File managedFileOne = entity.managedFile( MyEnum.fileOne );
            // END SNIPPET: api
            Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( managedFileOne ) );
        }
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new UoWFileAssembler().assemble( module );

        module.entities( TestedEntity.class ).withMixins( TestedFilesLocatorMixin.class );
        // END SNIPPET: assembly
        module.services( TestService.class );
        new EntityTestAssembler().assemble( module );
        new FileConfigurationAssembler().assemble( module );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    private TestService testService;

    @Before
    public void beforeTest()
    {
        testService = module.<TestService>findService( TestService.class ).get();
    }

    @Test
    public void testCreation()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Creation ##############################################################################" );
        File attachedFile;

        // Test discarded creation
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Creation Rollback" );
            attachedFile = entity.attachedFile( MyEnum.fileOne );
        }
        assertFalse( "File still exists after discarded creation UoW", attachedFile.exists() );

        // Test completed creation
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Creation" );
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }
        assertTrue( "File content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Creation" ) );
    }

    @Test
    public void testModification()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Modification ##########################################################################" );
        final String entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Modification" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        // Testing discarded modification
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            testService.modifyFile( entityId );
        }
        assertTrue( "File content after discarded modification was not the good one", isFileFirstLineEqualsTo( attachedFile, "Creation" ) );

        // Testing completed modification
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            testService.modifyFile( entityId );
            uow.complete();
        }
        assertTrue( "Modified file content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Modification" ) );
    }

    @Test
    public void testDeletion()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Deletion ##############################################################################" );
        final String entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Deletion" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        // Testing discarded deletion
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            TestedEntity entity = uow.get( TestedEntity.class, entityId );
            uow.remove( entity );
        }
        assertTrue( "File do not exists after discarded deletion", attachedFile.exists() );

        // Testing completed deletion
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            TestedEntity entity = uow.get( TestedEntity.class, entityId );
            uow.remove( entity );
            uow.complete();
        }
        assertFalse( "File still exists after deletion", attachedFile.exists() );
    }

    @Test
    public void testConcurrentModification()
        throws IOException, UnitOfWorkCompletionException
    {
        LOGGER.info( "# Test Concurrent Modification ###############################################################" );
        final String entityId;

        // Create new
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Concurrent Modification" );
            entityId = entity.identity().get();
            uow.complete();
        }

        // Testing concurrent modification
        UnitOfWork uow, uow2;
        TestedEntity entity;

        uow = uowf.newUnitOfWork();
        entity = uow.get( TestedEntity.class, entityId );
        Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile( MyEnum.fileOne ) ) );

        uow2 = uowf.newUnitOfWork();
        entity = uow2.get( TestedEntity.class, entityId );
        Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile( MyEnum.fileOne ) ) );

        uow.complete();
        try
        {
            uow2.complete();
            fail( "A ConcurrentUoWFileModificationException should have been raised" );
        }
        catch( ConcurrentUoWFileModificationException expected )
        {
            uow2.discard();
        }
    }

    @Test
    public void testRetry()
        throws IOException, UnitOfWorkCompletionException, InterruptedException
    {
        LOGGER.info( "# Test Retry #################################################################################" );
        final String entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Concurrent Modification" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        final List<Exception> ex = new ArrayList<>();
        Thread t1 = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    testService.modifyFileWithRetry( entityId, 0, 10000 );
                }
                catch( Exception ex1 )
                {
                    ex.add( ex1 );
                }
            }
        }, "job1" );
        Thread t2 = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    testService.modifyFileWithRetry( entityId, 5000, 0 );
                }
                catch( Exception ex1 )
                {
                    ex.add( ex1 );
                }
            }
        }, "job2" );

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        for( Exception eachEx : ex )
        {
            eachEx.printStackTrace();
        }

        assertTrue( "There were errors during TestRetry", ex.isEmpty() );
        assertTrue( "Modified file content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Modification" ) );
    }

    private TestedEntity createTestedOneEntityTwoFilesEntity( UnitOfWork uow, String name )
        throws IOException
    {
        EntityBuilder<TestedEntity> builder = uow.newEntityBuilder( TestedEntity.class );
        TestedEntity entity = builder.instance();
        entity.name().set( name );
        entity = builder.newInstance();
        Inputs.text( CREATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile( MyEnum.fileOne ) ) );
        return entity;
    }

}
