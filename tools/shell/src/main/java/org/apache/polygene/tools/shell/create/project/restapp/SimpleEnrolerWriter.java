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

package org.apache.polygene.tools.shell.create.project.restapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.polygene.tools.shell.FileUtils;

import static java.lang.String.format;

public class SimpleEnrolerWriter
{

    public void writeClass( Map<String, String> properties )
        throws IOException
    {
        String rootPackage = properties.get( "root.package" );
        try (PrintWriter pw = createPrinter( properties ))
        {
            pw.print( "package " );
            pw.print( properties.get( "root.package" ) );
            pw.println( ".rest.security;" );
            pw.println();
            pw.println(
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "import org.apache.polygene.api.injection.scope.Service;\n" +
                "import org.apache.polygene.api.injection.scope.Uses;\n" +
                "import org.restlet.Application;\n" +
                "import org.restlet.data.ClientInfo;\n" +
                "import org.restlet.security.Enroler;\n" +
                "import org.restlet.security.Role;" );
            pw.println( format( "import %s.model.security.SecurityRepository;\n", rootPackage ));
            pw.println();
            pw.println(
                "public class SimpleEnroler\n" +
                "    implements Enroler\n" +
                "{\n" +
                "    @Service\n" +
                "    private SecurityRepository repository;\n" +
                "\n" +
                "    @Uses\n" +
                "    private Application application;\n" +
                "\n" +
                "    @Override\n" +
                "    public void enrole( ClientInfo clientInfo )\n" +
                "    {\n" +
                "        org.restlet.security.User user = clientInfo.getUser();\n" +
                "        String name = user.getName();\n" +
                "        List<String> roleList = repository.findRoleNamesOfUser( name );\n" +
                "        List<Role> restletRoles = new ArrayList<>();\n" +
                "        roleList.stream().map( roleName -> Role.get( application, roleName ) );\n" +
                "        clientInfo.setRoles( restletRoles );\n" +
                "    }\n" +
                "}\n"
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/rest/security/";
        String classname = "SimpleEnroler";
        return FileUtils.createJavaClassPrintWriter( properties, "rest", packagename, classname );
    }
}
