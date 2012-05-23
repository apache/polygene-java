/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.uowfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.api.unitofwork.concern.UnitOfWorkRetry;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.io.Inputs;
import org.qi4j.io.Outputs;
import org.qi4j.library.uowfile.bootstrap.UoWFileAssembler;
import org.qi4j.library.uowfile.internal.ConcurrentUoWFileModificationException;
import org.qi4j.library.uowfile.singular.HasUoWFileLifecycle;
import org.qi4j.library.uowfile.singular.UoWFileLocator;
import org.qi4j.test.EntityTestAssembler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HasUoWFileTest
        extends AbstractUoWFileTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( HasUoWFileTest.class );

    private static final URL CREATION_CONTENT_URL = HasUoWFileTest.class.getResource( "creation.txt" );

    private static final URL MODIFICATION_CONTENT_URL = HasUoWFileTest.class.getResource( "modification.txt" );

    // START SNIPPET: entity
    // START SNIPPET: uowfile
    public interface TestedEntity
            extends EntityComposite,
                    // END SNIPPET: entity
                    HasUoWFileLifecycle
    // START SNIPPET: entity
    {

        Property<String> name();

    }
    // END SNIPPET: entity
    // END SNIPPET: uowfile

    // START SNIPPET: locator
    public static abstract class TestedFileLocatorMixin
            implements UoWFileLocator
    {

        @This
        private Identity meAsIdentity;

        @Override
        public File locateAttachedFile()
        {
            return new File( baseTestDir, meAsIdentity.identity().get() );
        }

    }
    // END SNIPPET: locator

    @Mixins( HasUoWFileTest.TestServiceMixin.class )
    @Concerns( UnitOfWorkConcern.class )
    public interface TestService
            extends ServiceComposite
    {

        void modifyFile( String entityId )
                throws IOException;

        @UnitOfWorkPropagation
        @UnitOfWorkRetry
        void modifyFileWithRetry( String entityId, long sleepBefore, long sleepAfter )
                throws IOException;

    }

    public static abstract class TestServiceMixin
            implements TestService
    {

        @Structure
        private Module module;

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
            if ( sleepBefore > 0 ) {
                try {
                    Thread.sleep( sleepBefore );
                } catch ( InterruptedException ex ) {
                    throw new RuntimeException( ex );
                }
            }
            modifyFileImmediatly( entityId );
            LOGGER.info( "Waiting " + sleepAfter + "ms after file modification" );
            if ( sleepAfter > 0 ) {
                try {
                    Thread.sleep( sleepAfter );
                } catch ( InterruptedException ex ) {
                    throw new RuntimeException( ex );
                }
            }
        }

        private void modifyFileImmediatly( String entityId )
                throws IOException
        {
            TestedEntity entity = module.currentUnitOfWork().get( TestedEntity.class, entityId );
            // START SNIPPET: api
            File attachedFile = entity.attachedFile();
            File managedFile = entity.managedFile();
            // END SNIPPET: api
            Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( managedFile ) );
        }

    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new UoWFileAssembler().assemble( module );

        module.entities( TestedEntity.class ).withMixins( TestedFileLocatorMixin.class );
        // END SNIPPET: assembly
        module.services( TestService.class );
        new EntityTestAssembler().assemble( module );
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

        // Test discarded creation
        UnitOfWork uow = module.newUnitOfWork();
        TestedEntity entity = createTestedEntity( uow, "Testing Creation Rollback" );
        File attachedFile = entity.attachedFile();
        uow.discard();
        assertFalse( "File still exists after discarded creation UoW", attachedFile.exists() );

        // Test completed creation
        uow = module.newUnitOfWork();
        entity = createTestedEntity( uow, "Testing Creation" );
        attachedFile = entity.attachedFile();
        uow.complete();
        assertTrue( "File content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Creation" ) );
    }

    @Test
    public void testModification()
            throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Modification ##########################################################################" );

        // Create new
        UnitOfWork uow = module.newUnitOfWork();
        TestedEntity entity = createTestedEntity( uow, "Testing Modification" );
        String entityId = entity.identity().get();
        File attachedFile = entity.attachedFile();
        uow.complete();

        // Testing discarded modification
        uow = module.newUnitOfWork();
        testService.modifyFile( entityId );
        uow.discard();
        assertTrue( "File content after discarded modification was not the good one", isFileFirstLineEqualsTo( attachedFile, "Creation" ) );

        // Testing completed modification
        uow = module.newUnitOfWork();
        testService.modifyFile( entityId );
        uow.complete();
        assertTrue( "Modified file content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Modification" ) );
    }

    @Test
    public void testDeletion()
            throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Deletion ##############################################################################" );

        // Create new
        UnitOfWork uow = module.newUnitOfWork();
        TestedEntity entity = createTestedEntity( uow, "Testing Deletion" );
        String entityId = entity.identity().get();
        File attachedFile = entity.attachedFile();
        uow.complete();

        // Testing discarded deletion
        uow = module.newUnitOfWork();
        entity = uow.get( TestedEntity.class, entityId );
        uow.remove( entity );
        uow.discard();
        assertTrue( "File do not exists after discarded deletion", attachedFile.exists() );

        // Testing completed deletion
        uow = module.newUnitOfWork();
        entity = uow.get( TestedEntity.class, entityId );
        uow.remove( entity );
        uow.complete();
        assertFalse( "File still exists after deletion", attachedFile.exists() );
    }

    @Test
    public void testConcurrentModification()
            throws IOException, UnitOfWorkCompletionException
    {
        LOGGER.info( "# Test Concurrent Modification ###############################################################" );

        // Create new
        UnitOfWork uow = module.newUnitOfWork();
        TestedEntity entity = createTestedEntity( uow, "Testing Concurrent Modification" );
        String entityId = entity.identity().get();
        uow.complete();

        // Testing concurrent modification
        uow = module.newUnitOfWork();
        entity = uow.get( TestedEntity.class, entityId );
        Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile() ) );
        UnitOfWork uow2 = module.newUnitOfWork();
        entity = uow2.get( TestedEntity.class, entityId );
        Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile() ) );
        uow.complete();
        try {
            uow2.complete();
            fail( "A ConcurrentUoWFileModificationException should have been raised" );
        } catch ( ConcurrentUoWFileModificationException expected ) {
            uow2.discard();
        }
    }

    @Test
    public void testRetry()
            throws IOException, UnitOfWorkCompletionException, InterruptedException
    {
        LOGGER.info( "# Test Retry #################################################################################" );

        // Create new
        UnitOfWork uow = module.newUnitOfWork();
        TestedEntity entity = createTestedEntity( uow, "Testing Concurrent Modification" );
        final String entityId = entity.identity().get();
        File attachedFile = entity.attachedFile();
        uow.complete();

        final List<Exception> ex = new ArrayList<Exception>();
        Thread t1 = new Thread( new Runnable()
        {

            @Override
            public void run()
            {
                try {
                    testService.modifyFileWithRetry( entityId, 0, 3000 );
                } catch ( Exception ex1 ) {
                    ex.add( ex1 );
                }
            }

        }, "job1" );
        Thread t2 = new Thread( new Runnable()
        {

            @Override
            public void run()
            {
                try {
                    testService.modifyFileWithRetry( entityId, 1000, 0 );
                } catch ( Exception ex1 ) {
                    ex.add( ex1 );
                }
            }

        }, "job2" );

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        for ( Exception eachEx : ex ) {
            eachEx.printStackTrace();
        }

        assertTrue( "There were errors during TestRetry", ex.isEmpty() );
        assertTrue( "Modified file content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Modification" ) );
    }

    private TestedEntity createTestedEntity( UnitOfWork uow, String name )
            throws IOException
    {
        EntityBuilder<TestedEntity> builder = uow.newEntityBuilder( TestedEntity.class );
        TestedEntity entity = builder.instance();
        entity.name().set( name );
        entity = builder.newInstance();
        Inputs.text( CREATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile() ) );
        return entity;
    }

}
