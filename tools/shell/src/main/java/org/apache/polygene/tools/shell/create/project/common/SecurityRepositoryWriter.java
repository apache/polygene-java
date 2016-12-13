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

package org.apache.polygene.tools.shell.create.project.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.polygene.tools.shell.FileUtils;

import static java.lang.String.format;

public class SecurityRepositoryWriter
{

    public void writeClass( Map<String, String> properties )
        throws IOException
    {
        String rootPackage = properties.get( "root.package" );
        try (PrintWriter pw = createPrinter( properties ))
        {
            pw.print( "package " );
            pw.print( properties.get( "root.package" ) );
            pw.println( ".model.security;" );
            pw.println();
            pw.println(
                "import java.util.List;\n" +
                "import org.apache.polygene.api.concern.Concerns;\n" +
                "import org.apache.polygene.api.unitofwork.concern.UnitOfWorkConcern;\n" +
                "import org.apache.polygene.api.unitofwork.concern.UnitOfWorkPropagation;\n" +
                "\n" +
                "@Concerns( UnitOfWorkConcern.class )\n" +
                "public interface SecurityRepository\n" +
                "{\n" +
                "    @UnitOfWorkPropagation\n" +
                "    boolean verifyPassword( String user, String password );\n" +
                "\n" +
                "    @UnitOfWorkPropagation\n" +
                "    List<String> findRoleNamesOfUser( String name );\n" +
                "}\n"
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/model/security/";
        String classname = "SecurityRepository";
        return FileUtils.createJavaClassPrintWriter( properties, "model", packagename, classname );
    }
}
