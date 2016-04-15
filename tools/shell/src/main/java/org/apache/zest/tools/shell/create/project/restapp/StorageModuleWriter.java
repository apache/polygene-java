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

public class StorageModuleWriter
{

    public void writeClass( Map<String, String> properties )
        throws IOException
    {
        String rootPackage = properties.get( "root.package" );
        String projectName = properties.get( "project.name" );
        try (PrintWriter pw = createPrinter( properties ))
        {
            pw.print( "package " );
            pw.print( properties.get( "root.package" ) );
            pw.println( ".bootstrap.infrastructure;" );
            pw.println();
            pw.println(
                "import org.apache.zest.api.common.Visibility;\n" +
                "import org.apache.zest.bootstrap.AssemblyException;\n" +
                "import org.apache.zest.bootstrap.LayerAssembly;\n" +
                "import org.apache.zest.bootstrap.ModuleAssembly;\n" +
                "import org.apache.zest.bootstrap.layered.ModuleAssembler;\n" +
                "import org.apache.zest.entitystore.file.assembly.FileEntityStoreAssembler;\n" +
                "\n" +
                "public class StorageModule\n" +
                "    implements ModuleAssembler\n" +
                "{\n" +
                "    public static final String NAME = \"Storage Module\";\n" +
                "    private final ModuleAssembly configModule;\n" +
                "\n" +
                "    public StorageModule( ModuleAssembly configModule )\n" +
                "    {\n" +
                "        this.configModule = configModule;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )\n" +
                "        throws AssemblyException\n" +
                "    {\n" +
                "\n" +
                "        new FileEntityStoreAssembler()\n" +
                "            .visibleIn( Visibility.application  )\n" +
                "            .withConfig( configModule, Visibility.application )\n" +
                "            .identifiedBy( \"filestore\" )\n" +
                "            .assemble( module );\n" +
                "        return module;\n" +
                "    }\n" +
                "}\n"
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/bootstrap/infrastructure/";
        String classname = "StorageModule";
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "bootstrap/src/main/java/" + packagename + classname + ".java" ) ) );
    }
}
