package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "rest/src/main/java/" + packagename + classname + ".java" ) ) );
    }
}
