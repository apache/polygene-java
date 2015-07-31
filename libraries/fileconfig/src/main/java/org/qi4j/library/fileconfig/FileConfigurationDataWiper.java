/*
 * Copyright 2013, Paul Merlin.
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
package org.qi4j.library.fileconfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.activation.ActivationEvent;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.structure.Application;

public final class FileConfigurationDataWiper
{

    public static void registerApplicationPassivationDataWiper( FileConfiguration fileConfig, Application application )
    {
        final List<File> dataDirectories = new ArrayList<File>();
        dataDirectories.add( fileConfig.configurationDirectory() );
        dataDirectories.add( fileConfig.cacheDirectory() );
        dataDirectories.add( fileConfig.dataDirectory() );
        dataDirectories.add( fileConfig.logDirectory() );
        dataDirectories.add( fileConfig.temporaryDirectory() );
        application.registerActivationEventListener( new ActivationEventListener()
        {
            @Override
            public void onEvent( ActivationEvent event )
            {
                if( event.type() == ActivationEvent.EventType.PASSIVATED && Application.class.isAssignableFrom( event.source().getClass() ) )
                {
                    for( File dataDir : dataDirectories )
                    {
                        if( !delete( dataDir ) )
                        {
                            System.err.println( "Unable to delete " + dataDir );
                        }
                    }
                }
            }
        } );
    }

    private static boolean delete( File file )
    {
        if( !file.exists() )
        {
            return true;
        }
        if( file.isFile() )
        {
            return file.delete();
        }
        else
        {
            for( File childFile : file.listFiles() )
            {
                if( !delete( childFile ) )
                {
                    return false;
                }
            }
            return file.delete();
        }
    }

    private FileConfigurationDataWiper()
    {
    }

}
