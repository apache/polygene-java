package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static java.lang.String.format;

public class ApplicationWriter
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
            pw.println( ".app;" );
            pw.println();
            pw.println( "import java.lang.reflect.UndeclaredThrowableException;" );
            pw.println( "import org.apache.zest.api.structure.Application;" );
            pw.println( "import org.apache.zest.bootstrap.AssemblyException;" );
            pw.println( "import org.apache.zest.bootstrap.layered.LayeredApplicationAssembler;" );
            pw.println( "import org.apache.zest.library.restlet.ZrestApplication;" );
            pw.println( "import org.restlet.Context;" );
            pw.println( "import org.restlet.routing.Filter;" );
            pw.println( "import org.restlet.routing.Router;" );
            pw.println( "import org.restlet.security.Enroler;" );
            pw.println( "import org.restlet.security.Verifier;" );
            pw.println( format( "import %s.bootstrap.%sApplicationAssembler;", rootPackage, projectName ) );
            pw.println( format( "import %s.bootstrap.connectivity.ConnectivityLayer;", rootPackage ) );
            pw.println( format( "import %s.bootstrap.connectivity.RestModule;", rootPackage ) );
            pw.println( format( "import %s.model.orders.Order;", rootPackage ) );
            pw.println( format( "import %s.model.orders.Customer;", rootPackage ) );
            pw.println(format("import %s.rest.security.SimpleEnroler;", rootPackage));
            pw.println(format("import %s.rest.security.SimpleVerifier;", rootPackage));
            pw.println();
            pw.println( format( "public class %s extends ZrestApplication", projectName ) );
            pw.println( "{\n" );
            pw.println( format( "    public %s( Context context )", projectName ) );
            pw.println( "        throws AssemblyException" );
            pw.println( "    {" );
            pw.println( "        super( context );" );
            pw.println( "    }\n" );
            pw.println( "    @Override" );
            pw.println( "    protected void addRoutes( Router router )" );
            pw.println( "    {" );
            pw.println( "        addResourcePath( \"orders\", Order.class, \"/orders/{order}/\", true, false );" );
            pw.println( "        addResourcePath( \"customers\", Customer.class, \"/customer/{customer}/\", true, false );" );
            pw.println( "    }" );

            pw.println( "    @Override" );
            pw.println( "    protected LayeredApplicationAssembler createApplicationAssembler( String mode )" );
            pw.println( "        throws AssemblyException" );
            pw.println( "    {" );
            pw.println( format("        return new %sApplicationAssembler( Application.Mode.valueOf( mode ) );", projectName) );
            pw.println( "    }" );
            pw.println();
            pw.println( "    @Override" );
            pw.println( "    protected Verifier createVerifier()" );
            pw.println( "    {" );
            pw.println( "        return newObject( SimpleVerifier.class );" );
            pw.println( "    }" );
            pw.println();
            pw.println( "    @Override" );
            pw.println( "    protected Enroler createEnroler()" );
            pw.println( "    {" );
            pw.println( "        return newObject( SimpleEnroler.class, this );" );
            pw.println( "    }" );
            pw.println();
            pw.println( "    @Override" );
            pw.println( "    protected String getConnectivityLayer()" );
            pw.println( "    {" );
            pw.println( "        return ConnectivityLayer.NAME;" );
            pw.println( "    }" );
            pw.println();
            pw.println( "    @Override" );
            pw.println( "    protected String getConnectivityModule()" );
            pw.println( "    {" );
            pw.println( "        return RestModule.NAME;" );
            pw.println( "    }" );
            pw.println();
            pw.println( "    private <T> T newObject( Class<T> type, Object... uses )" );
            pw.println( "    {" );
            pw.println( "        try" );
            pw.println( "        {" );
            pw.println( "            T instamce = type.newInstance();" );
            pw.println( "            objectFactory.injectTo( instamce, uses );" );
            pw.println( "            return instamce;" );
            pw.println( "        }" );
            pw.println( "        catch( Exception e )" );
            pw.println( "        {" );
            pw.println( "            throw new UndeclaredThrowableException( e );" );
            pw.println( "        }" );
            pw.println( "    }" );
            pw.println( "}" );
            pw.println();
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/app/";
        String classname = properties.get("project.name");
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "app/src/main/java/" + packagename + classname + ".java" ) ) );
    }
}
