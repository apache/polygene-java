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
package org.apache.polygene.library.fileconfig;

import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileConfigurationTest
{
    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

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
        assertNotNull( config.configurationDirectory() );
        assertNotNull( config.dataDirectory() );
        assertNotNull( config.temporaryDirectory() );
        assertNotNull( config.cacheDirectory() );
        assertNotNull( config.logDirectory() );
        System.out.println( "FileConfiguration defaults:\n"
                            + "\tconfiguration: " + config.configurationDirectory().getAbsolutePath() + "\n"
                            + "\tdata:          " + config.configurationDirectory().getAbsolutePath() + "\n"
                            + "\ttemporary:     " + config.configurationDirectory().getAbsolutePath() + "\n"
                            + "\tcache:         " + config.configurationDirectory().getAbsolutePath() + "\n"
                            + "\tlog:           " + config.configurationDirectory().getAbsolutePath() );
    }

    @Test
    public void testFileConfigurationOverride()
        throws IOException, ActivationException, AssemblyException
    {
        File testFile = tmpDir.getRoot();
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
                FileConfigurationOverride override = new FileConfigurationOverride()
                    .withConfiguration( confDir )
                    .withData( dataDir )
                    .withTemporary( tempDir )
                    .withCache( cacheDir )
                    .withLog( logDir );
                new FileConfigurationAssembler().withOverride( override ).assemble( module );
                // END SNIPPET: override
            }
        };

        FileConfiguration config = assembler.module().findService( FileConfiguration.class ).get();

        assertEquals( testFile.getAbsolutePath(), config.configurationDirectory().getAbsolutePath() );
        assertEquals( testFile.getAbsolutePath(), config.dataDirectory().getAbsolutePath() );
        assertEquals( testFile.getAbsolutePath(), config.temporaryDirectory().getAbsolutePath() );
        assertEquals( testFile.getAbsolutePath(), config.cacheDirectory().getAbsolutePath() );
        assertEquals( testFile.getAbsolutePath(), config.logDirectory().getAbsolutePath() );
    }

    @Test
    public void testFileConfigurationOverrideConvention()
        throws IOException, ActivationException, AssemblyException
    {
        File rootDir = tmpDir.getRoot();
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                // START SNIPPET: override-convention
                new FileConfigurationAssembler()
                    .withOverride( new FileConfigurationOverride().withConventionalRoot( rootDir ) )
                    .assemble( module );
                // END SNIPPET: override-convention
            }
        };

        FileConfiguration config = assembler.module().findService( FileConfiguration.class ).get();

        assertEquals( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_CONFIGURATION ).getAbsolutePath(),
                      config.configurationDirectory().getAbsolutePath() );
        assertEquals( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_DATA ).getAbsolutePath(),
                      config.dataDirectory().getAbsolutePath() );
        assertEquals( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_TEMPORARY ).getAbsolutePath(),
                      config.temporaryDirectory().getAbsolutePath() );
        assertEquals( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_CACHE ).getAbsolutePath(),
                      config.cacheDirectory().getAbsolutePath() );
        assertEquals( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_LOG ).getAbsolutePath(),
                      config.logDirectory().getAbsolutePath() );
    }
}
