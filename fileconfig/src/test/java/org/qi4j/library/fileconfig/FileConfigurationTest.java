/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.library.fileconfig;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import org.junit.Test;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class FileConfigurationTest
{

    @Test
    public void testFileConfiguration()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.services( FileConfiguration.class );
            }

        };

        FileConfiguration config = (FileConfiguration) assembler.module().findService( FileConfiguration.class ).get();

        File confDir = config.configurationDirectory();
        System.out.println( confDir );
    }

    @Test
    public void testFileConfigurationOverride()
            throws IOException
    {
        final File testFile = File.createTempFile( FileConfigurationTest.class.getName(), "" + System.currentTimeMillis() );
        SingletonAssembler assembler = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                FileConfigurationOverride override = new FileConfigurationOverride().withConfiguration( testFile ).
                        withData( testFile ).
                        withTemporary( testFile ).
                        withCache( testFile ).
                        withLog( testFile );
                module.services( FileConfiguration.class ).setMetaInfo( override );
            }

        };

        FileConfiguration config = ( FileConfiguration ) assembler.module().findService( FileConfiguration.class ).get();

        assertEquals( testFile.getAbsolutePath(), config.configurationDirectory().getAbsolutePath() );

    }

}
