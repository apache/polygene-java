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

package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class SettingsWriter
{

    public void writeClass( Map<String, String> properties )
        throws IOException
    {
        String projectName = properties.get( "project.name" );
        try (PrintWriter pw = createPrinter( properties ))
        {
            pw.println(
                String.format(
                    "\n" +
                    "include 'app',\n" +
                    "        'bootstrap',\n" +
                    "        'model',\n" +
                    "        'rest'\n" +
                    "\n" +
                    "rootProject.name = \"%s\"\n" +
                    "\n" +
                    "validateProject(rootProject, \"\")\n" +
                    "\n" +
                    "def validateProject(project, parentName)\n" +
                    "{\n" +
                    "  assert project.projectDir.isDirectory()\n" +
                    "  if( new File(\"$project.projectDir/src/main/java\").exists() )\n" +
                    "  {\n" +
                    "    assert project.buildFile.isFile()\n" +
                    "  }\n" +
                    "  if( parentName.length() > 0 )\n" +
                    "  println \"Project: \" + project.name\n" +
                    "  project.children.each { child ->\n" +
                    "    validateProject(child, project.name)\n" +
                    "  }\n" +
                    "}\n" +
                    "\n", projectName
                ) );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        File projectDir = new File( properties.get( "project.dir" ) );
        if( !projectDir.exists() )
        {
            if( !projectDir.mkdirs() )
            {
                System.err.println( "Unable to create directory: " + projectDir.getAbsolutePath() );
            }
        }
        return new PrintWriter( new FileWriter( new File( projectDir, "settings.gradle" ) ) );
    }
}
