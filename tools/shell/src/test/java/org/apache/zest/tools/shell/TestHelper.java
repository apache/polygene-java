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

public class TestHelper
{
    public static void setZestZome()
    {
        String cwd = new File( ".").getAbsolutePath();
        if( cwd.endsWith( "/java/." )) // IDEA default runner
        {
            String zestHome = new File( new File(".").getAbsoluteFile(), "tools/shell/src/dist" ).getAbsolutePath();
            System.setProperty( "zest.home", zestHome );
        }
        if( cwd.endsWith( "tools/shell/." )) // Gradle build
        {
            String zestHome = new File( new File(".").getAbsoluteFile(), "src/dist" ).getAbsolutePath();
            System.setProperty( "zest.home", zestHome );
        }
        if( cwd.endsWith( "test/work/." ) ) // Parallel Gradle build
        {
            String zestHome = new File( cwd + "./../../../../src/dist" ).getAbsolutePath();
            System.setProperty( "zest.home", zestHome );
        }
    }
}
