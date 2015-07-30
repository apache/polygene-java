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
package org.apache.zest.library.fileconfig;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;

import static org.junit.Assert.assertEquals;

public class FileConfigurationTest
{
    @Test
    public void testFileConfiguration()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                // START SNIPPET: simple
                new FileConfigurationAssembler().assemble( module );
                // END SNIPPET: simple
            }
        };

        FileConfiguration config = assembler.module().findService( FileConfiguration.class ).get();

        File confDir = config.configurationDirectory();
        System.out.println( confDir );
    }

    @Test
    public void testFileConfigurationOverride()
        throws IOException, ActivationException, AssemblyException
    {
        File testFile = File.createTempFile( FileConfigurationTest.class.getName(), "" + System.currentTimeMillis() );
        final File confDir = testFile;
        final File dataDir = testFile;
        final File tempDir = testFile;
        final File cacheDir = testFile;
        final File logDir = testFile;
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                // START SNIPPET: override
                FileConfigurationOverride override = new FileConfigurationOverride().
                    withConfiguration( confDir ).
                    withData( dataDir ).
                    withTemporary( tempDir ).
                    withCache( cacheDir ).
                    withLog( logDir );
                new FileConfigurationAssembler().withOverride( override ).assemble( module );
                // END SNIPPET: override
            }
        };

        FileConfiguration config = assembler.module().findService( FileConfiguration.class ).get();

        assertEquals( testFile.getAbsolutePath(), config.configurationDirectory().getAbsolutePath() );
    }
}
