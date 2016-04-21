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

import static java.lang.String.format;

public class OrderModuleWriter
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
            pw.println( ".bootstrap.domain;" );
            pw.println();
            pw.println(
                "import org.apache.zest.api.common.Visibility;\n" +
                "import org.apache.zest.bootstrap.AssemblyException;\n" +
                "import org.apache.zest.bootstrap.LayerAssembly;\n" +
                "import org.apache.zest.bootstrap.ModuleAssembly;\n" +
                "import org.apache.zest.bootstrap.layered.ModuleAssembler;");
            pw.println(format("import %s.model.orders.Order;", rootPackage));
            pw.println(format("import %s.model.orders.OrderItem;", rootPackage));
            pw.println(format("import %s.model.orders.Customer;", rootPackage));
            pw.println();
            pw.println(
                "public class OrderModule\n" +
                "    implements ModuleAssembler\n" +
                "{\n" +
                "    public static String NAME;\n" +
                "\n" +
                "    @Override\n" +
                "    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )\n" +
                "        throws AssemblyException\n" +
                "    {\n" +
                "        module.withDefaultUnitOfWorkFactory();\n" +
                "        module.values( /* add value types */    );\n" +
                "        module.entities( Customer.class, Order.class, OrderItem.class );\n" +
                "        module.services( /* add services */    )\n" +
                "            .visibleIn( Visibility.layer )\n" +
                "            .instantiateOnStartup();\n" +
                "        return module;\n" +
                "    }\n" +
                "}\n"
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/bootstrap/domain/";
        String classname = "OrderModule";
        return FileUtils.createJavaClassPrintWriter( properties, "bootstrap", packagename, classname );
    }
}
