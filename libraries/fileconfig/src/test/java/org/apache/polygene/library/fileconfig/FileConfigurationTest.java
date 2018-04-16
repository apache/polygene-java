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
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.test.TemporaryFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@ExtendWith( TemporaryFolder.class )
public class FileConfigurationTest
{
    private TemporaryFolder tmpDir;

    @Test
    public void testFileConfiguration()
        throws ActivationException
    {
        SingletonAssembler assembler = new SingletonAssembler(
            module -> {
                // START SNIPPET: simple
                new FileConfigurationAssembler().assemble( module );
                // END SNIPPET: simple
            }
        );

        FileConfiguration config = assembler.module().findService( FileConfiguration.class ).get();
        assertThat( config.configurationDirectory(), notNullValue() );
        assertThat( config.dataDirectory(), notNullValue() );
        assertThat( config.temporaryDirectory(), notNullValue() );
        assertThat( config.cacheDirectory(), notNullValue() );
        assertThat( config.logDirectory(), notNullValue() );
        System.out.println( "FileConfiguration defaults:\n"
                            + "\tconfiguration: " + config.configurationDirectory().getAbsolutePath() + "\n"
                            + "\tdata:          " + config.configurationDirectory().getAbsolutePath() + "\n"
                            + "\ttemporary:     " + config.configurationDirectory().getAbsolutePath() + "\n"
                            + "\tcache:         " + config.configurationDirectory().getAbsolutePath() + "\n"
                            + "\tlog:           " + config.configurationDirectory().getAbsolutePath() );
    }

    @Test
    public void testFileConfigurationOverride()
        throws IOException, ActivationException
    {
        File testFile = tmpDir.getRoot();
        final File confDir = testFile;
        final File dataDir = testFile;
        final File tempDir = testFile;
        final File cacheDir = testFile;
        final File logDir = testFile;
        SingletonAssembler assembler = new SingletonAssembler(
            module -> {
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
        );

        FileConfiguration config = assembler.module().findService( FileConfiguration.class ).get();

        assertThat( config.configurationDirectory().getAbsolutePath(), equalTo( testFile.getAbsolutePath() ) );
        assertThat( config.dataDirectory().getAbsolutePath(), equalTo( testFile.getAbsolutePath() ) );
        assertThat( config.temporaryDirectory().getAbsolutePath(), equalTo( testFile.getAbsolutePath() ) );
        assertThat( config.cacheDirectory().getAbsolutePath(), equalTo( testFile.getAbsolutePath() ) );
        assertThat( config.logDirectory().getAbsolutePath(), equalTo( testFile.getAbsolutePath() ) );
    }

    @Test
    public void testFileConfigurationOverrideConvention()
        throws IOException, ActivationException
    {
        File rootDir = tmpDir.getRoot();
        SingletonAssembler assembler = new SingletonAssembler(
            module -> {
                // START SNIPPET: override-convention
                new FileConfigurationAssembler()
                    .withOverride( new FileConfigurationOverride().withConventionalRoot( rootDir ) )
                    .assemble( module );
                // END SNIPPET: override-convention
            }
        );

        FileConfiguration config = assembler.module().findService( FileConfiguration.class ).get();

        assertThat( config.configurationDirectory(), equalTo( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_CONFIGURATION ) ) );
        assertThat( config.dataDirectory(), equalTo( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_DATA ) ) );
        assertThat( config.temporaryDirectory(), equalTo( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_TEMPORARY ) ) );
        assertThat( config.cacheDirectory(), equalTo( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_CACHE ) ) );
        assertThat( config.logDirectory(), equalTo( new File( rootDir, FileConfigurationOverride.CONVENTIONAL_LOG ) ) );
    }
}
