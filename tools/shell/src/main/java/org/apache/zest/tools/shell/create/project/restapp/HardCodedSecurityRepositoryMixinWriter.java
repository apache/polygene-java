package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "model/src/main/java/" + packagename + classname + ".java" ) ) );
    }
}
