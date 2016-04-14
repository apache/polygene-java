package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
                "import org.apache.zest.api.injection.scope.Service;\n" +
                "import org.apache.zest.api.injection.scope.Uses;\n" +
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
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "rest/src/main/java/" + packagename + classname + ".java" ) ) );
    }
}
