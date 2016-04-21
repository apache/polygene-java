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

package org.apache.zest.tools.shell.create.project.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.zest.tools.shell.FileUtils;

public class HardCodedSecurityRepositoryMixinWriter
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
                "import java.util.Collections;\n" +
                "import java.util.List;\n" +
                "import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;\n" +
                "\n" +
                "public class HardcodedSecurityRepositoryMixin\n" +
                "    implements SecurityRepository\n" +
                "{\n" +
                "\n" +
                "    @Override\n" +
                "    public boolean verifyPassword( String userName, String password )\n" +
                "    {\n" +
                "        if( userName.equals(\"admin\") && password.equals(\"secret\") )" +
                "        {\n" +
                "            return true;\n" +
                "        }\n" +
                "        if( userName.equals(\"user\") && password.equals(\"123\") )" +
                "        {\n" +
                "            return true;\n" +
                "        }\n" +
                "        return false;\n" +
                "    }\n" +
                "\n" +
                "    @UnitOfWorkPropagation\n" +
                "    public List<String> findRoleNamesOfUser( String name )\n" +
                "    {\n" +
                "        if( \"admin\".equals( name ) )\n" +
                "        {\n" +
                "            return Collections.singletonList(\"admin\");\n" +
                "        }\n" +
                "        return Collections.singletonList(\"user\");\n" +
                "    }\n" +
                "}\n"
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/model/security/";
        String classname = "HardcodedSecurityRepositoryMixin";
        return FileUtils.createJavaClassPrintWriter( properties, "model", packagename, classname );
    }
}
