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

public class ApplicationAssemblerWriter
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
            pw.println( ".bootstrap;" );
            pw.println();
            pw.println( "import java.io.IOException;" );
            pw.println( "import java.nio.file.Files;" );
            pw.println( "import java.nio.file.Path;" );
            pw.println( "import java.nio.file.Paths;" );
            pw.println( "import java.util.function.Function;" );
            pw.println();
            pw.println( "import org.apache.zest.api.structure.Application;" );
            pw.println( "import org.apache.zest.api.structure.Module;\n" );
            pw.println( "import org.apache.zest.bootstrap.ApplicationAssembly;" );
            pw.println( "import org.apache.zest.bootstrap.AssemblyException;" );
            pw.println( "import org.apache.zest.bootstrap.LayerAssembly;" );
            pw.println( "import org.apache.zest.bootstrap.ModuleAssembly;" );
            pw.println( "import org.apache.zest.bootstrap.layered.LayeredApplicationAssembler;" );
            pw.println();
            pw.println( "import " + rootPackage + ".bootstrap.connectivity.ConnectivityLayer;" );
            pw.println( "import " + rootPackage + ".bootstrap.domain.DomainLayer;" );
            pw.println( "import " + rootPackage + ".bootstrap.config.ConfigurationLayer;" );
            pw.println( "import " + rootPackage + ".bootstrap.infrastructure.InfrastructureLayer;" );
            pw.println();
            pw.print( "public class " );
            pw.print( projectName );
            pw.println( "ApplicationAssembler extends LayeredApplicationAssembler" );
            pw.println( "{" );
            pw.print( "    private static final String NAME = \"" );
            pw.print( projectName );
            pw.println( "\";" );
            pw.println( "    private static final String VERSION = \"1.0.alpha\";" );
            pw.println();
            pw.print("    public ");
            pw.print( projectName );
            pw.println("ApplicationAssembler( Application.Mode mode )");
            pw.println("    throws AssemblyException");
            pw.println("    {");
            pw.println("        super( NAME, VERSION, mode );");
            pw.println("    }");
            pw.println();
            pw.println("    @Override");
            pw.println("    protected void assembleLayers( ApplicationAssembly assembly )");
            pw.println("        throws AssemblyException");
            pw.println("    {");
            pw.println("        LayerAssembly configLayer = createLayer( ConfigurationLayer.class );");
            pw.println("        ModuleAssembly configModule = assemblerOf( ConfigurationLayer.class ).configModule();");
            pw.println("        LayerAssembly domainLayer = createLayer( DomainLayer.class );");
            pw.println("        Function<Application, Module> typeFinder = DomainLayer.typeFinder();");
            pw.println("        LayerAssembly infraLayer = new InfrastructureLayer( configModule, typeFinder ).assemble( assembly.layer( InfrastructureLayer.NAME ) );");
            pw.println("        LayerAssembly connectivityLayer = createLayer( ConnectivityLayer.class );");
            pw.println("        connectivityLayer.uses( domainLayer );");
            pw.println("        domainLayer.uses( infraLayer );");
            pw.println("        infraLayer.uses( configLayer );");
            pw.println("    }");
            pw.println("}");
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/bootstrap/";
        String classname = properties.get( "project.name" ) + "ApplicationAssembler";
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "bootstrap/src/main/java/" + packagename + classname + ".java" ) ));
    }
}
