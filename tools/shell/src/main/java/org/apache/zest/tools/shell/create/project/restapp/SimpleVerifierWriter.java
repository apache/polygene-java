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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.zest.tools.shell.FileUtils;

import static java.lang.String.format;

public class SimpleVerifierWriter
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
                "import org.apache.zest.api.injection.scope.Service;\n" +
                "import org.restlet.security.SecretVerifier;\n" +
                "import org.restlet.security.Verifier;\n" +
                format( "import %s.model.security.SecurityRepository;\n", rootPackage ) +
                "\n" +
                "public class SimpleVerifier extends SecretVerifier\n" +
                "    implements Verifier\n" +
                "{\n" +
                "    @Service\n" +
                "    private SecurityRepository repository;\n" +
                "\n" +
                "    @Override\n" +
                "    public int verify( String user, char[] secret )\n" +
                "    {\n" +
                "        if( user == null || secret == null )\n" +
                "        {\n" +
                "            return RESULT_UNKNOWN;\n" +
                "        }\n" +
                "        if( repository.verifyPassword( user, String.valueOf( secret ) ) )\n" +
                "        {\n" +
                "            return RESULT_VALID;\n" +
                "        }\n" +
                "        return RESULT_INVALID;\n" +
                "    }\n" +
                "}\n"
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/rest/security/";
        String classname = "SimpleVerifier";
        return FileUtils.createJavaClassPrintWriter( properties, "rest", packagename, classname );
    }
}
