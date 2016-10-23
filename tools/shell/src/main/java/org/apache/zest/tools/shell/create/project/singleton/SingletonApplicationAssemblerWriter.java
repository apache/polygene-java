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

package org.apache.zest.tools.shell.create.project.singleton;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.zest.tools.shell.FileUtils;

import static java.lang.String.format;

public class SingletonApplicationAssemblerWriter
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
            pw.println( ".app;" );
            pw.println();
            pw.println( "import org.apache.zest.api.structure.Application;" );
            pw.println( "import org.apache.zest.bootstrap.AssemblyException;" );
            pw.println();
            pw.println( format( "public class %s", projectName ) );
            pw.println( "{\n" );
            pw.println( "    public static void main( String[] args )");
            pw.println( "        throws Exception" );
            pw.println( "    {" );
            pw.println( "        SingletonAssembly app = new SingletonAssembly()" );
            pw.println( "        {" );
            pw.println( "            @Override\n" +
                        "            public void assemble( ModuleAssembly module )\n" +
                        "                throws AssemblyException\n" +
                        "            {\n" +
                        "                module.values( );\n" +
                        "                module.entities( );\n" +
                        "                module.services( StartupService.class );\n" +
                        "                    .instantiateOnStartup();\n" +
                        "            }\n" );
            pw.println( "        }" );
            pw.println();
            pw.println( "        registerShutdownHook();" );
            pw.println( "        StartupService startup = app.serviceFinder().findService( StartupService.class );" );
            pw.println();
            pw.println( "        // Here the application is ready to do something." );
            pw.println( "        // You should add functionality in the StartupService class." );
            pw.println( "    }" );
            pw.println( );
            pw.println( "    private void registerShutdownHook()" );
            pw.println( "    {" );
            pw.println( "        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()\n" +
                        "        {\n" +
                        "            @Override\n" +
                        "            public void run()\n" +
                        "            {\n" +
                        "                try\n" +
                        "                {\n" +
                        "                    app.passivate();\n" +
                        "                }\n" +
                        "                catch( Exception e )\n" +
                        "                {\n" +
                        "                    System.err.println( \"Clean Shutdown  of application failed.\" );\n" +
                        "                    e.printStackTrace();\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }, \"Shutdown Hook for Zest\" ) );\n" );
            pw.println( "    }" );
            pw.println( "}" );
            pw.println();
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" );
        String classname = properties.get("project.name");
        return FileUtils.createJavaClassPrintWriter( properties, "", packagename, classname );
    }
}
