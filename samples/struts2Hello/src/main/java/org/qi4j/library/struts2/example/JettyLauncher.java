package org.qi4j.library.struts2.example;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyLauncher
{

    public static void main( String[] args )
    {
        Server server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort( 8080 );
        server.addConnector( connector );

        WebAppContext web = new WebAppContext( server, "struts2.example", "/example" );
        web.setWar( "libraries/struts2/example/src/main/webapp/" );

        try
        {
            server.start();
            server.join();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.exit( 100 );
        }
    }
}
