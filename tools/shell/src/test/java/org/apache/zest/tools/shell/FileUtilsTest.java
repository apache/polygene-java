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

package org.apache.zest.tools.shell;

import java.io.File;
import java.util.Map;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class FileUtilsTest
{
    @Test
    public void createDirectoryTest()
    {
        File f = new File( "habba-zout" );
        if( f.exists() )
        {
            FileUtils.removeDir( f );
        }
        assertThat( f.exists(), equalTo( false ) );
        FileUtils.createDir( "habba-zout" );
        assertThat( f.exists(), equalTo( true ) );
        if( ! f.delete() ){
            System.err.println( "Unable to remove file. Why???" );
        }
        assertThat( f.exists(), equalTo( false ) );
    }

    @Test
    public void removeDirTest() throws Exception {
        File srcFile = new File("build.gradle");
        File f = new File( "habba-zout" );
        if( f.exists() )
        {
            FileUtils.removeDir( f );
        }
        assertThat( f.exists(), equalTo( false ) );
        File f1 = FileUtils.createDir( "habba-zout" );
        File f2 = FileUtils.createDir( "habba-zout/src" );
        File f3 = FileUtils.createDir( "habba-zout/src/main" );
        File f4 = FileUtils.createDir( "habba-zout/src/test" );
        File f5 = FileUtils.createDir( "habba-zout/src/main/abc" );
        FileUtils.copyFile( srcFile, new File(f1, "build.gradle__") );
        FileUtils.copyFile( srcFile, new File(f2, "build.gradle__") );
        FileUtils.copyFile( srcFile, new File(f3, "build.gradle__") );
        FileUtils.copyFile( srcFile, new File(f4, "build.gradle__") );
        FileUtils.copyFile( srcFile, new File(f5, "build.gradle__") );
        boolean success = FileUtils.removeDir( f );
        assertThat(success, equalTo(true));
        assertThat(f1.exists(), equalTo(false));
        assertThat(f2.exists(), equalTo(false));
        assertThat(f3.exists(), equalTo(false));
        assertThat(f4.exists(), equalTo(false));
        assertThat(f5.exists(), equalTo(false));
    }

    @Test
    public void readPropertiesResourceTest()
    {
        TestHelper.zetZestZome();
        Map<String, String> map = FileUtils.readTemplateProperties( "restapp" );
        assertThat( map, notNullValue() );
        assertThat( map.get( "template.dir" ), equalTo( "etc/templates/restapp/files" ) );
    }

    @Test
    public void copyFileTest()
        throws Exception
    {
        File dest = new File( "build.gradle.copy" );
        if( dest.exists() ) // from an earlier aborted run.
        {
            if( ! dest.delete() ){
                System.err.println( "Unable to remove file. Why???" );
            }
        }
        File srcFile = new File( "build.gradle" );
        FileUtils.copyFile( srcFile, dest );
        assertThat( dest.exists(), equalTo( true ) );
        if( ! dest.delete() ){
            System.err.println( "Unable to remove file. Why???" );
        }
        assertThat( dest.exists(), equalTo( false ) );
    }
}
