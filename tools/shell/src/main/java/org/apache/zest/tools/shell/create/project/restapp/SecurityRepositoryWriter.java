package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
                "import org.apache.zest.api.concern.Concerns;\n" +
                "import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;\n" +
                "import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;\n" +
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
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "model/src/main/java/" + packagename + classname + ".java" ) ) );
    }
}
