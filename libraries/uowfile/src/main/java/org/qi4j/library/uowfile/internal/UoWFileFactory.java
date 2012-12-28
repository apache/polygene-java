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
package org.qi4j.library.uowfile.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( UoWFileFactory.Mixin.class )
@Activators( UoWFileFactory.Activator.class )
public interface UoWFileFactory
        extends ServiceComposite
{

    UoWFile createCurrentUoWFile( File file );

    void ensureWorkDirsExist()
            throws IOException;

    class Activator
            extends ActivatorAdapter<ServiceReference<UoWFileFactory>>
    {

        @Override
        public void afterActivation( ServiceReference<UoWFileFactory> activated )
                throws Exception
        {
            activated.get().ensureWorkDirsExist();
        }

    }

    abstract class Mixin
            implements UoWFileFactory
    {

        private static class UoWFilesMetaInfo
                extends HashMap<String, UoWFile>
        {
        }

        private static final Logger LOGGER = LoggerFactory.getLogger( "org.qi4j.library.uowfile" );

        @Structure
        private Application app;

        @Structure
        private Module module;

        @This
        private ServiceComposite me;

        @Optional
        @Service
        private FileConfiguration fileConfig;

        private File workDir;

        @Override
        public void ensureWorkDirsExist()
                throws IOException
        {
            File tmp;
            if ( fileConfig == null ) {
                tmp = new File( "qi4j", app.name() + "-" + app.version() );
            } else {
                tmp = fileConfig.temporaryDirectory();
            }
            workDir = new File( tmp, "uowfile-" + me.identity().get() );
            if ( !workDir.exists() && !workDir.mkdirs() ) {
                throw new IOException( "Unable to create temporary directory: " + workDir );
            }
        }

        @Override
        public UoWFile createCurrentUoWFile( File file )
        {
            return createUoWFile( module.currentUnitOfWork(), file, workDir );
        }

        private static synchronized UoWFile createUoWFile( UnitOfWork uow, File file, File workDir )
        {
            UoWFilesMetaInfo uowMeta = ensureUoWMeta( uow );
            String absolutePath = file.getAbsolutePath();
            UoWFile uowFile = uowMeta.get( absolutePath );
            if ( uowFile == null ) {
                uowFile = new UoWFile( file, workDir );
                uowFile.copyOriginalToCurrent();
                uowMeta.put( absolutePath, uowFile );
                LOGGER.trace( "Registered {} in UoW", uowFile );
            }
            return uowFile;
        }

        /**
         * Ensure UoW meta info tracking UoWFiles is present and UoW callback is registered.
         */
        private static UoWFilesMetaInfo ensureUoWMeta( final UnitOfWork uow )
        {
            UoWFilesMetaInfo uowMeta = uow.metaInfo( UoWFilesMetaInfo.class );
            if ( uowMeta != null ) {
                return uowMeta;
            }

            uowMeta = new UoWFilesMetaInfo();
            uow.setMetaInfo( uowMeta );

            uow.addUnitOfWorkCallback( new UnitOfWorkCallback()
            {

                @Override
                public void beforeCompletion()
                        throws UnitOfWorkCompletionException
                {
                    UoWFilesMetaInfo uowMeta = uow.metaInfo( UoWFilesMetaInfo.class );
                    if ( uowMeta != null && !uowMeta.isEmpty() ) {
                        List<UoWFile> concurrentlyModified = new ArrayList<UoWFile>();
                        for ( UoWFile eachUoWFile : uowMeta.values() ) {
                            try {
                                eachUoWFile.apply();
                            } catch ( ConcurrentUoWFileStateModificationException ex ) {
                                concurrentlyModified.add( ex.getUoWFile() );
                            }
                        }
                        if ( !concurrentlyModified.isEmpty() ) {
                            throw new ConcurrentUoWFileModificationException( concurrentlyModified );
                        }
                    }
                }

                @Override
                public void afterCompletion( UnitOfWorkStatus status )
                {
                    UoWFilesMetaInfo uowMeta = uow.metaInfo( UoWFilesMetaInfo.class );
                    if ( uowMeta != null && !uowMeta.isEmpty() ) {
                        for ( UoWFile eachUoWFile : uowMeta.values() ) {
                            if ( status == UnitOfWorkStatus.DISCARDED ) {
                                eachUoWFile.rollback();
                            }
                            eachUoWFile.cleanup();
                        }
                        uow.metaInfo( UoWFilesMetaInfo.class ).clear();
                    }
                }

            } );
            return uowMeta;
        }

    }

}
