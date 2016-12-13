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

package org.apache.zest.tools.shell.create;

import java.io.File;
import java.io.IOException;
import org.apache.zest.tools.shell.HelpNeededException;
import org.apache.zest.tools.shell.TestHelper;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CreateProjectTest
{
    private CreateProject underTest = new CreateProject();

    @Test
    public void givenCommandWhenRequestNameExpectName()
    {
        assertThat( underTest.name(), equalTo( "create-project" ) );
    }

    @Test
    public void givenCommandWhenRequestDescriptionExpectDescription()
    {
        assertThat( underTest.description(), equalTo( "type name package\tCreates a new skeletal project in directory [name]." ) );
    }

    @Test( expected = HelpNeededException.class )
    public void givenCommandWhenTemplateDoesNotExistExpectException()
    {
        new CreateProject().execute( new String[]{ "habba", "PolygeneTest", "org.apache.zest" }, null, null );
    }

    @Test
    public void givenCommandWhenTemplateExistExpectCreatedProject() throws IOException
    {
        TestHelper.setPolygeneZome();
        File dest = new File( "PolygeneTest" );
        new CreateProject().execute( new String[]{ "create-project", "null", "PolygeneTest", "org.apache.zest" }, null, null );

        assertThat( dest.exists(), equalTo( true ) );
        File file = new File( dest, "src/main/java/org/apache/zest/package.html" );
        assertThat( file.exists(), equalTo( true ) );
    }
}
