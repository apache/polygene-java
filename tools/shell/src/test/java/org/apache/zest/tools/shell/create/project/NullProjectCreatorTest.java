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

package org.apache.zest.tools.shell.create.project;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.zest.tools.shell.FileUtils;
import org.apache.zest.tools.shell.TestHelper;
import org.junit.Test;

import static org.apache.zest.tools.shell.FileUtils.removeDir;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NullProjectCreatorTest
{
    private NullProjectCreator underTest = new NullProjectCreator();

    @Test
    public void givenCorrectInputWhenCreatedProjectExpectCompleteProjectCreated()
        throws Exception
    {

        TestHelper.zetZestZome();
        File projectDir = new File( "ZestTest" );
        if( projectDir.exists() )
        {
            removeDir( projectDir );
        }
        Map<String, String> properties = new HashMap<>();
        properties.put( "zest.home", System.getProperty( "zest.home" ) );
        properties.put( "root.package", "org.apache.zest.test" );
        properties.put( "template.dir", "etc/templates/null/files" );
        underTest.create( "ZestTest", projectDir, properties );

        assertThat( projectDir.exists(), equalTo( true ) );
        assertThat( new File( projectDir, "src/main/java/org/apache/zest/test/package.html" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "gradlew" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "gradlew.bat" ).exists(), equalTo( true ) );
        assertThat( new File( projectDir, "build.gradle" ).exists(), equalTo( true ) );
        if( FileUtils.removeDir( projectDir ) )
        {
            System.err.println( "Unable to remove file. Why???" );
        }
        assertThat( projectDir.exists(), equalTo( false ) );
    }
}
